import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import LoadingBox from '../components/LoadingBox';
import MessageBox from '../components/MessageBox';
import { listOrderMine } from '../actions/orderActions';
export default function OrderHostoryScreen(props){
    const orderMineList = useSelector(state => state.orderMineList);
    const{ orders, loading, error}= orderMineList;
    const dispatch = useDispatch();
    useEffect(()=>{
        dispatch(listOrderMine());
    },[dispatch])
    return(

        <div>
            <h1>Order History</h1>
            {
                loading? <LoadingBox></LoadingBox>:
                error? <MessageBox variant ="danger"></MessageBox>:
                (
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Order Id</th>
                            <th>Name</th>
                            <th>Date</th>
                            <th>Total</th>
                            <th>Paid</th>
                            <th>Delivered</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                       <tbody>
                           {orders.map((order)=>(
                               <tr key={order._id}>
                                   
                                   <td>{order._id}</td>
                                   <td>{order.orderItems.map((item) => (item.name))}</td>
                                   <td>{order.createdAt.substring(0,10)}</td>
                                   <td>₹ {order.totalPrice}</td>
                                   <td>{order.isPaid? order.paidAt.substring(0,10): 'NO'}</td>
                                   <td>{order.isDelivered? order.deliveredAt.substring(0,10): 'NO'}</td>
                                   <td>
                                       <button className="small"
                                       type="button"
                                       onClick={()=>{props.history.push(`/order/${order._id}`)}}
                                       >Details</button>
                                   </td>
                               </tr>
                           ))}
                       </tbody>
                    </table>
                )
            }
        </div>
    )
}