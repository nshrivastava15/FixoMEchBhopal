import React, { useEffect, useState } from 'react';
import Product from '../components/Product';
import axios from 'axios';
export default function HomeScreen() {
    /**
     * defining hooks
     */
const [products, setproducts] = useState([]);
useEffect(()=>{
const fetchData = async() =>{
    const {data}= await axios.get('api/products');
    setproducts(data);
};
fetchData();
},[])

    return (
        <div className="row center">
        {products.map(product => (
          <Product key={product._id} product={product}></Product>
        ))}
      </div>
    )
}