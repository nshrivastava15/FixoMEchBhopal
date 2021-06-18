import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { signin } from '../actions/UserActions';
import LoadingBox from '../components/LoadingBox';
import MessageBox from '../components/MessageBox';
export default function SigninScreen(props) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const userSignin = useSelector((state) => state.userSignin);
    const { userInfo, error, loading } = userSignin;
const redirect = props.location.search? 
props.location.search.split('=')[1]: 
'/';

    const dispatch = useDispatch();
    const submitHandler = (e) => {
        e.preventDefault();
        dispatch(signin(email, password));
    };

    useEffect(()=>{
        if(userInfo){
            props.history.push(redirect);
        }
    },[props.history, redirect, userInfo]);
    return (
        <form className="form" onSubmit={submitHandler}>
            <div>Sign In</div>
            {loading && <LoadingBox></LoadingBox>}
            {error && <MessageBox variant="danger">{error}</MessageBox>}
            <div>
                <label htmlFor="email">Email Address</label>
                <input  type="email" id="email"  required placeholder="Enter Registered Email"
                    onChange={(e) => setEmail(e.target.value)}></input>
            </div>
            <div>
                <label htmlFor="password">Password</label>
                <input id="password" type="password" required placeholder="Enter Password"
                    onChange={(e) => setPassword(e.target.value)}></input>
            </div>
            <div>
                <label />
                <button className="primary" type="submit">Sign In</button>
            </div>
            <div>
                <label />
                <div>
                    New Customer?{' '}
                    <Link to={`/register?redirect?${redirect}`}>Create Your Account</Link>
                </div>
            </div>
        </form>
    )
}