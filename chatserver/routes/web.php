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

