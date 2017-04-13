<?php

namespace App\Http\Controllers;

use JWTAuth;

use App\Models\User;
use App\Models\Message;
use App\Models\Challenge;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class DemoController extends Controller {

public function demo(Request $request)
{
	return response()->json(['message'=>'this is demo']);

}
}

?>