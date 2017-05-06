<?php

namespace App\Models;


use Illuminate\Database\Eloquent\Model;

class TeamMessage extends Model
{
	
	
	protected $fillable = ['message', 'from', 'group_id','group_name','to'];
    	protected $hidden = [];




}
?>