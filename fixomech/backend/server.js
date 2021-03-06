import express from 'express';
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import productRouter from './routers/productRouter.js';
import userRouter from './routers/userRouter.js';
import orderRouter from './routers/orderRouter.js';

dotenv.config();
const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true}));

// eslint-disable-next-line no-undef
mongoose.connect(process.env.MONDODB_URL || 'mongodb://localhost/fixomech', {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    useCreateIndex: true,
});
app.use('/api/users', userRouter);
app.use('/api/products', productRouter);
app.use('/api/orders', orderRouter );
app.get('/api/config/paypal', (req, res)=>{
    // eslint-disable-next-line no-undef
    res.send(process.env.PAYPAL_CLIENT_ID || 'sb');
})
app.get('/', (req, res) => {
    console.log("in orders api");
    res.send("Server is Ready");
});
app.use((err, req, res, next) => {
    res.status(500).send({ error: err.message });
});
app.listen(5000, () => {
    console.log(`Server started at 5000`);
});