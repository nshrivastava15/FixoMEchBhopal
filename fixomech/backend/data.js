
import bcrypt from 'bcryptjs';

const data = {
    users: [
        {
            name: 'Nikhil',
            email: 'admin@fixomech.com',
            password: bcrypt.hashSync('1234', 8),
            isAdmin: true,
        },
        {
            name: 'Akshay',
            email: 'akshay@fixomech.com',
            password: bcrypt.hashSync('1234', 8),
            isAdmin: false,
        }
    ],
    products: [
        {
            name: 'Fan Service',
            category: 'service',
            image: '/images/service-1.jpg',
            price: 120,
            countInStock: 0,
            brand: 'fixomech',
            rating: 4.5,
            numReviews: 10,
            description: 'high quaity product',
        },
        {
            name: 'AC Repair Service',
            category: 'service',
            image: '/images/service-1.jpg',
            price: 120,
            countInStock: 4,
            brand: 'fixomech',
            rating: 3,
            numReviews: 10,
            description: 'high quaity product',
        },
        {
            name: 'Washing Machine Service',
            category: 'service',
            image: '/images/service-1.jpg',
            price: 160,
            countInStock: 6,
            brand: 'fixomech',
            rating: 4.5,
            numReviews: 10,
            description: 'high quaity product',
        },
        {
            name: 'TV Service',
            category: 'service',
            image: '/images/service-1.jpg',
            price: 150,
            countInStock: 7,
            brand: 'fixomech',
            rating: 4.5,
            numReviews: 10,
            description: 'high quaity product',
        },
        {
            name: 'Wifi Service',
            category: 'service',
            image: '/images/service-1.jpg',
            price: 140,
            countInStock: 0,
            brand: 'fixomech',
            rating: 4.5,
            numReviews: 10,
            description: 'high quaity product',
        },
        {
            name: 'Laptop Service',
            category: 'service',
            image: '/images/service-1.jpg',
            price: 140,
            countInStock: 0,
            brand: 'fixomech',
            rating: 4.5,
            numReviews: 10,
            description: 'high quaity product',
        }
    ]
}

export default data;