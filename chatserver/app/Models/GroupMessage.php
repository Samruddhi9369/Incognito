<?php

namespace App\Models;


use Illuminate\Database\Eloquent\Model;

class GroupMessage extends Model
{
	
	
	protected $fillable = ['message', 'from', 'group_id'];
    	protected $hidden = [];




}
?>