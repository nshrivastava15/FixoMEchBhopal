import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { register } from '../actions/UserActions';
import LoadingBox from '../components/LoadingBox';
import MessageBox from '../components/MessageBox';
export default function RegisterScreen(props) {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const userRegister = useSelector((state) => state.userSignin);
    const { userInfo, error, loading } = userRegister;
    const redirect = props.location.search ?
        props.location.search.split('=')[1] :
        '/';

    const dispatch = useDispatch();
    const submitHandler = (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            alert('Password and Confirm Password does not macth');
        } else {
            dispatch(register(name, email, password));
        }
    };

    useEffect(() => {
        if (userInfo) {
            props.history.push(redirect);
        }
    }, [props.history, redirect, userInfo]);
    return (
        <form className="form" onSubmit={submitHandler}>
            <div>Create Account By Sharing Few Details</div>
            {loading && <LoadingBox></LoadingBox>}
            {error && <MessageBox variant="danger">{error}</MessageBox>}
            <div>
                <label htmlFor="name">Name</label>
                <input type="name" id="name" required placeholder="Enter Your Name"
                    onChange={(e) => setName(e.target.value)}></input>
            </div>
            <div>
                <label htmlFor="email">Email Address</label>
                <input type="email" id="email" required placeholder="Enter Registered Email"
                    onChange={(e) => setEmail(e.target.value)}></input>
            </div>
            <div>
                <label htmlFor="password">Password</label>
                <input id="password" type="password" required placeholder="Enter Password"
                    onChange={(e) => setPassword(e.target.value)}></input>
            </div>
            <div>
                <label htmlFor="confirmPassword">Confirm Password</label>
                <input id="confirmPassword" type="confirmPassword" required placeholder="Confirm Password"
                    onChange={(e) => setConfirmPassword(e.target.value)}></input>
            </div>
            <div>
                <label />
                <button className="primary" type="submit">Register</button>
            </div>
            <div>
                <label />
                <div>
                    Already a Member?{' '}
                    <Link to={`/signin?redirect?${redirect}`}>Sing In here</Link>
                </div>
            </div>
        </form>
    )
}