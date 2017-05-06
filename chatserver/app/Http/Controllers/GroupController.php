<?php

namespace App\Http\Controllers;

use JWTAuth;

use App\Models\User;
use App\Models\Message;
use App\Models\Group;
use App\Models\GroupMessage;
use App\Models\TeamMessage;
use App\Models\Challenge;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class GroupController extends Controller {
	
	
		/* --------------------------------------------------	
			Function for Group Creation
		Input Params : group name, group admin, group members
		Error code 0 -> User is not authorized
		Error code 1 -> Group name already exists
		Error code 2 -> Missing Parameters
		Error code 3 -> Group created successfully
		------------------------------------------------------ */
	
     public function createGroup(Request $request) {
        //JWTAuth::parseToken()->authenticate();
        $parameters = $request->all();
	    
	$group = Group::where('name', $parameters['name'])->first();
                if(!empty($group)) {
                    $errors = ['error' => 'Group name already exists', 'code' => '1'];
                    return response()->json($errors);
                } else {
                    $group = new Group;
                    $group->name = $parameters['name'];
                    $group->members = $parameters['members'];
                    $group->admin = $parameters['admin'];
		   // $group->keys = $parameters['keys'];	
                    $group->save();
		    $errors = ['error' => 'Group created', 'code' => '3'];
                    return response()->json($errors);
                }    
    }


		/* --------------------------------------------------	
			Function for Get Groups
		Input Params : member email
		Error code 0 -> User is not authorized
		Error code 1 -> Group name already exists
		Error code 2 -> Missing Parameters
		Error code 3 -> Group created successfully
		------------------------------------------------------ */
	
     public function getGroups(Request $request) {
        //JWTAuth::parseToken()->authenticate();
        $parameters = $request->all();
	$groups = Group::where('members','like','%'.$parameters['user_email'].'%')->where('id','>',$parameters['last_read'])->get();
        return response()->json(['groups'=>$groups]);

     }


		/* --------------------------------------------------	
			Function for Group Send Message
		Input Params : message,from,group_id
		Error code 0 -> Invalid sender ID
		Error code 1 -> Invalid Group ID
		Error code 2 -> Missing Parameters

		------------------------------------------------------ */

    public function sendGroupMessage(Request $request) {
        	
		//JWTAuth::parseToken()->authenticate();

		$parameters = $request->all();
		
		$group = Group::where('name', $parameters['group_name'])->first();

		if(empty($group)) {
			
			$errors = ['error' => 'Invalid Email ID', 'code' => '3'];
		   	return response()->json($errors);
		}

              	    $group_id = $group->id;
                
                    $message = new GroupMessage;
                    $message->message = $parameters['message'];
		    $message->from = $parameters['from'];
                    $message->group_id = $group_id;
                    $message->save();
		    $response = ['message' => 'Message Sent Successfully', 'code' => '3'];
                    return response()->json($response);   

		
                  
    }

		/* --------------------------------------------------	
			Function for Group get Messages
		Input Params : message,from,group_id
		Error code 0 -> Invalid sender ID
		Error code 1 -> Invalid Group ID
		Error code 2 -> Missing Parameters

		------------------------------------------------------ */

    public function getGroupMessage(Request $request) {
        //JWTAuth::parseToken()->authenticate();
        	$parameters = $request->all();

			$receiver=User::where('email',$parameters['from'])->first();
			if(empty($receiver) || ($receiver->is_verified==false))
			{
				$response=['error'=>'Invalid Receiver email','code'=>'0'];
				return response()->json($response);
			}else
			{
				$group_name=$parameters['group_name'];
				$group = Group::where('name', $group_name)->first();
                		$group_id = $group->id;

				$from=$parameters['from'];
				$lastread=$parameters['Last_Group_Read'];
				$message=GroupMessage::where(function($query) use ($group_id,$lastread){
						$query->where('group_id',$group_id)
						      ->where('id','>',$lastread);
				})->get();
				//$message=Message::where('to',$parameters['to'])->where('from',$parameters['from'])->where('id','>'$parameters->lastread)->orderBy('created_at','asc')->get();
				return response()->json(['messages'=>$message]);
				//return response()->json(['message'=>'error is here']);
			}
		
    }

		/* --------------------------------------------------	
			Function for Group get Details
		Input Params : message,from,group_id
		Error code 0 -> Invalid Group ID
		Error code 1 -> Invalid Group ID

		------------------------------------------------------ */
    /*public function getDetails(Request $request) {
        //JWTAuth::parseToken()->authenticate();
        $parameters = $request->all()['nameValuePairs'];
        if(array_has($parameters, 'id') && array_has($parameters, 'group_id')) {
            $group = Group::find($parameters['group_id']);
            if(empty($group)) {
                $errors = ['error' => 'Invalid group ID', 'code' => '0'];
                    return response()->json($errors);

            } else {
                $members = substr($group->members, 1, strlen($group->members) - 1);
                $members = explode(',', $members);
                $members = array_map('trim', $members);
                // print_r($members); exit;
                // Should be a member of the group to request the data.
                if(array_has($members, $parameters['id'])) {
                    return response()->json($group);
                } else {
		    $errors = ['error' => 'Invalid Group ID', 'code' => '1'];
                    return response()->json($errors);
                }
            }
        }
    }*/

		/* --------------------------------------------------	
			Function for Group Send Message
		Input Params : message,from,group_id
		Error code 0 -> Invalid sender ID
		Error code 1 -> Invalid Group ID
		Error code 2 -> Missing Parameters

		------------------------------------------------------ */

    public function sendTeamMessage(Request $request) {
        	
		//JWTAuth::parseToken()->authenticate();

		$parameters = $request->all();
		
		$group = Group::where('name', $parameters['group_name'])->first();

		if(empty($group)) {
			
			$errors = ['error' => 'Invalid Email ID', 'code' => '3'];
		   	return response()->json($errors);
		}

              	    $group_id = $group->id;
                
                    $message = new TeamMessage;
                    $message->message = trim($parameters['message']);
		    $message->from = $parameters['from'];
		    $message->to = $parameters['to'];
		    $message->group_name = $parameters['group_name'];			
                    $message->group_id = $group_id;
                    $message->save();
		    $response = ['message' => 'Message Sent Successfully', 'code' => '3'];
                    return response()->json($response);   

		
                  
    }

    public function getTeamMessage(Request $request) {
        //JWTAuth::parseToken()->authenticate();
        	$parameters = $request->all();

			$receiver=User::where('email',$parameters['to'])->first();
			if(empty($receiver) || ($receiver->is_verified==false))
			{
				$response=['error'=>'Invalid Receiver email','code'=>'0'];
				return response()->json($response);
			}else
			{
				$group_name=$parameters['group_name'];
				$group = Group::where('name', $group_name)->first();
                		$group_id = $group->id;

				//$from=$parameters['from'];
				$to=$parameters['to'];
				$lastread=$parameters['Last_Group_Read'];
				$message=TeamMessage::where(function($query) use ($group_id,$to,$lastread){
						$query->where('group_id',$group_id)
						     ->where('to',$to)
						     ->where('id','>',$lastread);
				})->get();
				//$message=Message::where('to',$parameters['to'])->where('from',$parameters['from'])->where('id','>'$parameters->lastread)->orderBy('created_at','asc')->get();
				return response()->json(['messages'=>$message]);
				//return response()->json(['message'=>'error is here']);
			}
		
    }

}
