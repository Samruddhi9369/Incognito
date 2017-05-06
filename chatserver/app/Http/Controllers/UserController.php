<?php


namespace App\Http\Controllers;

use PHPMailer;
use App\Models\User;
use App\Models\Challenge;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Tymon\JWTAuth\Exceptions\JWTException;
use Tymon\JWTAuth\Exceptions\TokenExpiredException;
use Tymon\JWTAuth\Exceptions\TokenInvalidException;
//use Tymon\JWTAuth\JWTAuth;
use JWTAuth;

class UserController extends Controller {
	

	protected $jwt;

    public function __construct(JWTAuth $jwt)
    {
        $this->jwt = $jwt;
    }
	

	
		/* --------------------------------------------------	
			Function for User Registration
		Input Params : firstname, lastname, email, phone, password
		------------------------------------------------------ */
	
	public function test(Request $request)
	{		
		
		$parameters = $request->all();
		$mandatoryFields = [
				'firstname' => 'required',
				'lastname' => 'required',
		        	'email' => 'required',
			        'phone' => 'required',
			        'password' => 'required'
		    ];
		$fieldscheck = Validator::make($parameters, $mandatoryFields);

		if ($fieldscheck->fails()) {
			$errors = ['error' => 'validation_failed', 'code' => '0'];

			foreach ((array)$fieldscheck->errors()->messages() as $key => $value) {
				$errors['fields'][] = $key;
			}
			return response()->json($errors);
		}
		$dupcheck=[
				'email'=>'unique:users,email'
		];
		$checker=Validator::make($parameters,$dupcheck);
		if($checker->fails()){
			$errors=['error'=>'Duplicate Email ID' , 'code'=>'10'];
			return response()->json($errors);
		}

		$user = User::firstOrNew(['email' => $parameters['email']]);
		$user->firstname=$parameters['firstname'];
		$user->lastname=$parameters['lastname'];
		$user->phone=$parameters['phone'];
		$user->salt=bin2hex(random_bytes(32));
		$password=$parameters['password'];
		//$user->password_hash=password_hash($user->salt.$password,PASSWORD_BCRYPT);
		$user->password_hash=hash('sha512',$user->salt.$password);
		$code = random_int(100000, 999999);
		$user->verification_code=$code;
		$user->save();

		/*--------------------------------------------------------
			 PHP Mail Function	
		ini_set("SMTP","ssl://smtp.gmail.com");
		ini_set("smtp_port","587");
		ini_set("sendmail_path ","D:\wamp64\sendmail\sendmail.exe");
		----------------------------------------------------------- */

		$to = "suya123.suyash@gmail.com";
	        $subject = "Incognito : Verification Code";
         
        	$message = "<b>Incognito : Secure Chat .</b>";
	        $message .= "Verification code for Incognito = ";
			$message .=$code;
         
        	$header = "From:suya123.suyash@gmail.com \r\n";
	        $header .= "Cc:\r\n";
        	$header .= "MIME-Version: 1.0\r\n";
	        $header .= "Content-type: text/html\r\n";

		$retval=mail($to,$subject,$message,$header);
		if( $retval == true ) {
			$response =['message'=>'User Registered Successfully! Verification Code sent!', 'code'=>'1'];
       		return response()->json($response);
         	}else {
			 $response=['message'=>'User Registered Failed!','code'=>'2'];
           		 return response()->json($response);		 
        	 }
													
							
					
	}


	/* Status Codes :
	   0 -> Validation Failed.
	   1 -> User Registered.
	   2 -> Registration Failed.	
	   3 -> Verification Failed. Invalid Email.
	   4 -> Succcessful Verification. Token Issued to the user.
	   5 -> Couldn't verify. Invalid Verification Code.
	   6 -> Missing Parameters in Login Step 1.
	   7 -> Invalid Login Email. No such User Found.
	   8 ->	Missing Parameters in Login Step 2.
	   9 -> Invalid Password.
	*/


	/* --------------------------------------------------------
		Function for User Verification
		Input Params : email , verification_code
	------------------------------------------------------------*/
	
	public function verifyUser(Request $request)
	{
		$params = $request->all();
		$email =$params['email'];
		//$user = User::find(1);
		$user = User::where('email','=',$email)->first();
		if(empty($user)) {
			
			$errors = ['error' => 'Invalid Email ID', 'code' => '3'];
		   	return response()->json($errors);
		}else{
			//echo($user->verification_code);
			if(strcmp($user->verification_code, $params['verification_code']) == 0) {
				$user->verification_code = '';
				$user->is_verified = true;
				$user->save();
				$token=JWTAuth::fromUser($user);
				//$token = $this->jwt->attempt($params);
				$user->token = compact('token')['token'];
				$response=['token'=>$token,'code'=>'4','ID'=>$user->id];
				return response()->json($response);
			} else {
				// Invalid verification code.
				$errors = ['error' => 'Invalid Verification Code', 'code' => '5'];
				return response()->json($errors);
			
			}
			

		}
				
	}


	/* ------------------------------------------
		Function for Remote Login
		Input Params part 1 : Email
		Input Params part 2 : Email,tag
	---------------------------------------------- */ 
	
	
	public function remoteLogin(Request $request, $part)
	{
		if($part==1)
		{
			$params = $request->all();
			$rules1 = [
			        'email' => 'required'
			    ];
			$validator1 = Validator::make($params, $rules1);
			if($validator1->fails()) {
				$errors = ['error' => 'Missing parameters', 'code' => '6'];
				return response()->json($errors);
			} else {
				$user = User::where('email', $params['email'])->first();
				if(empty($user)) {
					$errors = ['error' => 'Invalid Email', 'code' => '7'];
					return response()->json($errors);
				} else {
					$cval = Challenge::firstOrCreate(['email' => $params['email']]);
					$cval->challenge = bin2hex(random_bytes(32));
					$cval->save();
					
					$response =['c'=>$cval->challenge,'salt'=>$user->salt,'code'=>'0'];
					
					return response()->json($response);
				}
			}
			
		}else if($part==2)
		{
			$params = $request->all();
			$rules2 = [
			        'email' => 'required',
			        'tag' => 'required'
			    ];
			
			$validator1 = Validator::make($params, $rules2);

			if($validator1->fails()) {
				$errors = ['error' => 'Missing parameters', 'code' => '8'];
				return response()->json($errors);
			} else {
				$user = User::where('email', $params['email'])->first();
				$challenge = Challenge::where('email', $params['email'])->first();
				$tag=$params['tag'];
				$mytag=hash_hmac('sha512',$user->password_hash,$challenge->challenge);
				if(strcmp($mytag,$tag)==0)
				{
					$challenge->delete();
					$token=JWTAuth::fromUser($user);
					$user->token = compact('token')['token'];
					return response()->json(['token'=>$token,'code'=>'0','user_id'=>$user->id]);
				}else {
						$errors = ['error' => 'Invalid Password', 'code' => '9'];
						return response()->json($errors);
					}
			}
		}
		
	}

	public function getUser(Request $request) {
		
		/*
		if(!JWTAuth::parseToken()->authenticate())
		{
			$res=['error'=>'auth failed', 'code'=>'12'];
			return response()->json($res);
		}*/	
		
		$parameters = $request->all();
		// Sender should exist and be verified
		$id=$parameters['id'];
		$sender = User::find($parameters['sender_id']);
		if(empty($sender) || $sender->is_verified == false) 
		{
				$errors = ['error' => 'Invalid ID', 'code' => '0'];
				return response()->json($errors);
		} else 
		{
				// User should exist and be verified
				$user = User::find($id);
				if(empty($user) || $user->is_verified == false) {
					$errors = ['error' => 'Invalid ID', 'code' => '1'];
					return response()->json($errors);
				} else {
					$res=['user'=>$user,'code'=>'11'];
					return response()->json($res);
				}
			
		} 

					
	}
}
