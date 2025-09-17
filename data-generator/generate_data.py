#!/usr/bin/env python3

import json
import random
import time
from datetime import datetime, timedelta
from decimal import Decimal
from kafka import KafkaProducer
from faker import Faker
import argparse
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

fake = Faker()

class DataGenerator:
    def __init__(self, bootstrap_servers='localhost:9092'):
        self.producer = KafkaProducer(
            bootstrap_servers=bootstrap_servers,
            value_serializer=lambda v: json.dumps(v, default=str).encode('utf-8'),
            key_serializer=lambda k: str(k).encode('utf-8') if k else None
        )
        self.customer_ids = set()
        self.order_counter = 1
        
    def generate_customer(self, customer_id=None):
        """Generate a random customer record"""
        if customer_id is None:
            customer_id = random.randint(1, 10000)
            
        self.customer_ids.add(customer_id)
        
        customer = {
            "customer_id": customer_id,
            "name": fake.name(),
            "email": fake.email(),
            "phone": fake.phone_number(),
            "address": fake.address().replace('\n', ', '),
            "created_at": datetime.now().isoformat(),
            "updated_at": datetime.now().isoformat()
        }
        return customer
    
    def generate_order(self, customer_id=None):
        """Generate a random order record"""
        if customer_id is None:
            if not self.customer_ids:
                # Generate some customers first
                for _ in range(10):
                    self.customer_ids.add(random.randint(1, 1000))
            customer_id = random.choice(list(self.customer_ids))
        
        products = [
            "Laptop", "Smartphone", "Tablet", "Headphones", "Mouse", "Keyboard",
            "Monitor", "Webcam", "Speakers", "Printer", "Router", "Hard Drive",
            "Graphics Card", "RAM", "SSD", "Power Supply", "Motherboard", "CPU"
        ]
        
        quantity = random.randint(1, 5)
        unit_price = round(random.uniform(10.0, 2000.0), 2)
        total_amount = round(quantity * unit_price, 2)
        
        order = {
            "order_id": self.order_counter,
            "customer_id": customer_id,
            "product_name": random.choice(products),
            "quantity": quantity,
            "unit_price": unit_price,
            "total_amount": total_amount,
            "order_status": random.choice(["PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"]),
            "order_date": datetime.now().isoformat()
        }
        
        self.order_counter += 1
        return order
    
    def send_customer(self, customer):
        """Send customer record to Kafka"""
        try:
            self.producer.send('customers', key=customer['customer_id'], value=customer)
            logger.info(f"Sent customer: {customer['customer_id']} - {customer['name']}")
        except Exception as e:
            logger.error(f"Failed to send customer: {e}")
    
    def send_order(self, order):
        """Send order record to Kafka"""
        try:
            self.producer.send('orders', key=order['order_id'], value=order)
            logger.info(f"Sent order: {order['order_id']} - Customer: {order['customer_id']} - Product: {order['product_name']}")
        except Exception as e:
            logger.error(f"Failed to send order: {e}")
    
    def generate_initial_customers(self, count=100):
        """Generate initial batch of customers"""
        logger.info(f"Generating {count} initial customers...")
        for i in range(count):
            customer = self.generate_customer(customer_id=i+1)
            self.send_customer(customer)
            time.sleep(0.1)  # Small delay to avoid overwhelming
        
        self.producer.flush()
        logger.info(f"Generated {count} initial customers")
    
    def simulate_customer_updates(self, update_rate=0.1):
        """Simulate customer updates (upserts)"""
        if not self.customer_ids:
            return
            
        if random.random() < update_rate:
            customer_id = random.choice(list(self.customer_ids))
            customer = self.generate_customer(customer_id)
            customer['updated_at'] = datetime.now().isoformat()
            self.send_customer(customer)
    
    def run_continuous_generation(self, customers_per_minute=10, orders_per_minute=50, customer_update_rate=0.1):
        """Run continuous data generation"""
        logger.info("Starting continuous data generation...")
        logger.info(f"Target rates - Customers: {customers_per_minute}/min, Orders: {orders_per_minute}/min")
        
        customer_interval = 60.0 / customers_per_minute if customers_per_minute > 0 else float('inf')
        order_interval = 60.0 / orders_per_minute if orders_per_minute > 0 else float('inf')
        
        last_customer_time = 0
        last_order_time = 0
        
        try:
            while True:
                current_time = time.time()
                
                # Generate new customers
                if customers_per_minute > 0 and (current_time - last_customer_time) >= customer_interval:
                    customer = self.generate_customer()
                    self.send_customer(customer)
                    last_customer_time = current_time
                
                # Generate orders
                if orders_per_minute > 0 and (current_time - last_order_time) >= order_interval:
                    order = self.generate_order()
                    self.send_order(order)
                    last_order_time = current_time
                
                # Simulate customer updates
                self.simulate_customer_updates(customer_update_rate)
                
                time.sleep(0.1)  # Small sleep to prevent busy waiting
                
        except KeyboardInterrupt:
            logger.info("Stopping data generation...")
        finally:
            self.producer.flush()
            self.producer.close()

def main():
    parser = argparse.ArgumentParser(description='Generate sample data for Iceberg real-time ingestion')
    parser.add_argument('--bootstrap-servers', default='localhost:9092', 
                       help='Kafka bootstrap servers (default: localhost:9092)')
    parser.add_argument('--initial-customers', type=int, default=100,
                       help='Number of initial customers to generate (default: 100)')
    parser.add_argument('--customers-per-minute', type=int, default=10,
                       help='New customers per minute (default: 10)')
    parser.add_argument('--orders-per-minute', type=int, default=50,
                       help='Orders per minute (default: 50)')
    parser.add_argument('--customer-update-rate', type=float, default=0.1,
                       help='Customer update probability per cycle (default: 0.1)')
    parser.add_argument('--skip-initial', action='store_true',
                       help='Skip generating initial customers')
    
    args = parser.parse_args()
    
    generator = DataGenerator(args.bootstrap_servers)
    
    # Generate initial customers unless skipped
    if not args.skip_initial:
        generator.generate_initial_customers(args.initial_customers)
    
    # Run continuous generation
    generator.run_continuous_generation(
        customers_per_minute=args.customers_per_minute,
        orders_per_minute=args.orders_per_minute,
        customer_update_rate=args.customer_update_rate
    )

if __name__ == '__main__':
    main()
