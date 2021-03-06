<?php

namespace App\Http\Controllers;

use JWTAuth;

use App\Models\User;
use App\Models\Message;
use App\Models\Challenge;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class MessageController extends Controller {


	/* --------------------------------------------------
		Function to send one to one messages
		Input Parameters : sender, message, receiver
		
		Status Codes :
		0 -> Missing Values
		1 -> Invalid Sender Details 
		2 -> Invalid Receiver Details
	-----------------------------------------------------*/

	public function sendMessages(Request $request)
	{
		// Authenticate JWT Token
		if(!JWTAuth::parseToken()->authenticate())
		{
			$res=['error'=>'auth failed'];
			return response()->json($res);
		}
		$fields=$request->all();
		$paramscheck= [
			'sender' => 'required',
			'message' => 'required',
			'receiver' => 'required'
		];

		$validation= Validator::make($fields,$paramscheck);
		if($validation->fails())
		{
			$response=['error'=>'Missing Values', 'code'=>'0'];	
			return response()->json($response);
		}
		else
		{
			$user=User::where('email', $fields['sender'])->first();
			if(empty($user) || ($user->is_verified == false))
			{
				$response =['error'=>'Invalid Sender Details', 'code'=>'1'];
				return response()->json($response);
			}
			else{
				$receiver=User::where('email', $fields['receiver'])->first();
				if(empty($receiver) || ($receiver->is_verified == false))
				{	
					$response = ['error'=>'Invalid Receiver Details', 'code'=>'2'];
					return response()->json($response);
				}
				else{

					$message = new Message;
					$message->to= $fields['receiver'];
					$message->from = $fields['sender'];
					$message->message = trim($fields['message']);
					$message->save();
					
					return response()->json($message);


				}			
			
			}
			
			
		}
		

	}

	/* --------------------------------------------------
		Function to get received messages
		Input Parameters : from, to, lastread
		
		Status Codes :
		0 -> Invalid Sender email / receiver email

	-----------------------------------------------------*/


	public function getMessages(Request $request){
	
	// Authenticate JWT Token
		if(!JWTAuth::parseToken()->authenticate())
		{
			$error=['error' => 'Authentication Failed'];
			return response()->json($error);
		}
	
		$parameters = $request->all();

		/*$fieldcheck =[ 
			'from'=>'required',
			'lastread'=>'required',
			'to'=>required

		];*/	

		$sender=User::where('email',$parameters['from'])->first();
		if(empty($sender) || ($sender->is_verified==false))
		{
			$response=['error'=>'Invalid sender email','code'=>'0'];
			return response()->json($response);
		}else
		{
			$receiver=User::where('email',$parameters['to'])->first();
			if(empty($receiver) || ($receiver->is_verified==false))
			{
				$response=['error'=>'Invalid Receiver email','code'=>'0'];
				return response()->json($response);
			}else
			{
				$to=$parameters['to'];
				$from=$parameters['from'];
				$lastread=$parameters['lastread'];
				$message=Message::where(function($query) use ($to,$from,$lastread){
						$query->where('to',$to)
						      ->where('from',$from)
						      ->where('id','>',$lastread);
				})->get();
				//$message=Message::where('to',$parameters['to'])->where('from',$parameters['from'])->where('id','>'$parameters->lastread)->orderBy('created_at','asc')->get();
				return response()->json(['messages'=>$message]);
				//return response()->json(['message'=>'error is here']);
			}
		}

	}

	/* --------------------------------------------------
		Function to get received messages
		Input Parameters : Groupname, Receiver email id, last read
		
		Status Codes :
		0 -> Invalid receiver email

	-----------------------------------------------------*/


	public function getKeyMessages(Request $request){
	
	// Authenticate JWT Token
	/*	if(!JWTAuth::parseToken()->authenticate())
		{
			$error=['error' => 'Authentication Failed'];
			return response()->json($error);
		}*/
	
		$parameters = $request->all();

		/*$fieldcheck =[ 
			'from'=>'required',
			'lastread'=>'required',
			'to'=>required

		];*/	


			$receiver=User::where('email',$parameters['to'])->first();
			if(empty($receiver) || ($receiver->is_verified==false))
			{
				$response=['error'=>'Invalid Receiver email','code'=>'0'];
				return response()->json($response);
			}else
			{
				$group_name=$parameters['groupname'];
				$to=$parameters['to'];
				$lastread=$parameters['lastread'];
				$message=Message::where(function($query) use ($to,$group_name,$lastread){
						$query->where('to',$to)
						      ->where('message','like','%'.$group_name.'%')
						      ->where('id','>',$lastread);
				})->get();
				//$message=Message::where('to',$parameters['to'])->where('from',$parameters['from'])->where('id','>'$parameters->lastread)->orderBy('created_at','asc')->get();
				return response()->json(['keymessages'=>$message]);
				//return response()->json(['message'=>'error is here']);
			}
		
	}

}

?>