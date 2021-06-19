import  jwt  from 'jsonwebtoken';

/**
 * Middleware to generate Token
 * @param {*} user 
 * @returns 
 */
export const generateToken = (user) => {
  return  jwt.sign({
        _id: user._id,
        name: user.name,
        email: user.password,
        isAdmin: user.isAdmin,
    }, process.env.JWT_SECRET,
        {
            expiresIn: '30d',
        });
};
/**
 * Middleware to Authticate Valid User Request
 * @param {*} req 
 * @param {*} res 
 * @param {*} next 
 */
export const isAuth = (req,res,next)=>{
    const authorization = req.headers.authorization;
    if(authorization){
        const token = authorization.slice(7, authorization.length);
       jwt.verify(token, process.env.JWT_SECRET || 'somethingsecret', (error, decode)=>{
           if(error){
               res.status(401).send({message:'Invalid Token'});
           }
           else{
               req.user = decode;
               next();
           }
       });
    }else{
        res.status(401).send({message:'No Token Available'})
    }
};