import React from 'react';
import { BrowserRouter, Route } from 'react-router-dom';
import ProductScreen from './screens/ProductScreen';
import HomeScreen from './screens/HomeScreen';
import CartScreen from './screens/CartScreen';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import SigninScreen from './screens/SigninScreen';
import { signout } from './actions/UserActions';
import RegisterScreen from './screens/RegisterScreen';
import ShippingAddressScreen from './screens/ShippingAddressScreen';
import PaymentMethodScreen from './screens/PaymentMethodScreen';
import PlaceOrderScreen from './screens/PlaceOrderScreen';
import img from './images/fixomechLogo.png';
import OrderScreen from './screens/OrderScreen';
import OrderHostoryScreen from './screens/OrderHistoryScreen';
function App() {
  const cart = useSelector((state) => state.cart);
  const { cartItems } = cart;
  const userSignin = useSelector((state) => state.userSignin);
  const { userInfo } = userSignin;
  const dispatch = useDispatch();
  const signoutHandler = () => {
    dispatch(signout());
  }
  return (
    <BrowserRouter>
      <div className="grid-container">
        <header className="row header">
          <div>
            <Link className="brand" to="/"><img className="logo" src={img} alt="FIXOMECH"></img></Link>
          </div>
          <div>
            <Link to="/cart"><i className="fa fa-shopping-bag"></i>{
              cartItems.length > 0 && (
                <span className="badge">{cartItems.length}</span>
              )
            }</Link>
            {
              userInfo ?
                (
                  <div className="dropdown">
                    <Link to="#">{userInfo.name} <i className="fa fa-caret-down"></i></Link>
                    <ul className="dropdown-content">
                      <li>
                        <Link to="/orderhistory">Order History</Link>
                      </li>
                      <li><Link to="#signout" onClick={signoutHandler}>
                        Sign Out
                      </Link></li>
                      
                    </ul>
                  </div>
                ) :
                (<Link to="/signin">Sign In</Link>)
            }
          </div>
        </header>
        <hr/>
        <main>
          <div className="row">
            <div className="col-2">
            <img className="medium" src={img} alt="FIXOMECH"></img>
            </div>
            
          </div>
          <Route path="/cart/:id?" component={CartScreen}></Route>
          <Route path="/product/:id" component={ProductScreen}></Route>
          <Route path="/signin" component={SigninScreen}></Route>
          <Route path="/register" component={RegisterScreen}></Route>
          <Route path="/shipping" component={ShippingAddressScreen}></Route>
          <Route path="/payment" component={PaymentMethodScreen}></Route>
          <Route path="/placeorder" component={PlaceOrderScreen}></Route>
          <Route path="/order/:id" component={OrderScreen}></Route>
          <Route path="/orderhistory" component={OrderHostoryScreen}></Route>
          <Route path="/" component={HomeScreen} exact></Route>
        </main>
        <footer className="row center">All right reserved</footer>
      </div>
    </BrowserRouter>
  );
}

export default App;
