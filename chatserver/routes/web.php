<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It is a breeze. Simply tell Lumen the URIs it should respond to
| and give it the Closure to call when that URI is requested.
|
*/

$app->get('demo',function () {
	return response()->json(['m'=>'h']);

});

$app->get('/', function () use ($app) {
    return $app->version();
});

$app->post('register','UserController@test');				// first_name, last_name, email, phone, password ----> Create User.
$app->post('verify','UserController@verifyUser'); 			// email, verification_code ----> Verify User.
$app->post('login/{part}','UserController@remoteLogin'); 		// 1. email 2. email , tag  -----> Remote Login 2 parts.
$app->post('getuser','UserController@getUser');				// 1. sender_id  2. id  (JWT Token in Header) ------> Get User Details for Friend List.


$app->post('demo','DemoController@demo');

$app->post('send','MessageController@sendMessages');			// sender,message,receiver ------> Send Message 1 to 1.
$app->post('getMessage','MessageController@getMessages');		// from,lastread,to  ---------> Retrieve Messages 1 to 1. 

$app->post('createGroup','GroupController@createGroup');		// name, admin, members ------------> Create Group
$app->post('getGroups','GroupController@getGroups');			// member email ------------> Get Groups
$app->post('sendGroupMessage','GroupController@sendGroupMessage');	// message, sender, groupname ------------> send Group Message
$app->post('getGroupMessage','GroupController@getGroupMessage');	// sender, groupname ------------> get Group Message
$app->post('getKeyMessages','MessageController@getKeyMessages');	// to, groupname, lastread ------------> get Key Message

$app->post('sendTeamMessage','GroupController@sendTeamMessage');	// message,from,to,group_name ---------> send group messages
$app->post('getTeamMessage','GroupController@getTeamMessage');		// to,group_name,Last_Group_Read ---------> send group messages



