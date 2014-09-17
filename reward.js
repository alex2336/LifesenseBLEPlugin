var Category = Parse.Object.extend("RewardCategory");
var Reward = Parse.Object.extend("RewardType");
var Image = require( "parse-image" );

exports.index = function (req, res) {
	var currentUser = Parse.User.current();
	var category = req.query.category;
	var defaultId;
	console.log("CAT filter:"+category);
	var currentCategory;
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var query = new Parse.Query(Reward);
		query.include('category');
		query.find({
			success:function(rewards){
				var rewardsDisplay=[];
				rewards.forEach(function(reward){
					reward.categoryName = reward.get('category').get('name');
					reward.categoryId = reward.get('category').id;
					if(typeof category=="undefined"){
						category = reward.get('category').id;
					}
					console.log("1111:"+JSON.stringify(reward.get('category')));
					if(category!="all"&&typeof category!="undefined"&&reward.categoryId==category){
						defaultId=reward.get('category').get('defaultReward').id;
						rewardsDisplay.push(reward);
					}
					console.log(JSON.stringify(reward));
				});
				if(category=="all"||typeof category=="undefined"){
					rewardsDisplay=rewards;
				}else{
					currentCategory=category;
				}
				console.log(rewards);
				var query = new Parse.Query(Category);
				query.find({
					success:function(categories){
						console.log(JSON.stringify(rewards));
						res.render('reward/list', {rewards:rewardsDisplay,categories:categories,currentCategory:currentCategory,defaultId:defaultId});
					}
				})
				
			}
		});

	}
}
exports.createForm = function (req, res) {
	var currentUser = Parse.User.current();
	
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var query = new Parse.Query(Category);
		query.find({
			success:function(categories){
				console.log(JSON.stringify(categories));
				res.render('reward/createReward', {categories:categories});
			}
		});
	}
}
exports.setDefault = function (req, res) {
	var currentUser = Parse.User.current();
	
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var rewardId = req.query.id;
		var categoryId = req.query.categoryId;
		var query = new Parse.Query(Category);
		query.equalTo("objectId",categoryId);
		query.find({
			success:function(categories){
				var query = new Parse.Query(Reward);
				query.equalTo("objectId",rewardId);
				query.find({
					success:function(rewards){
						categories[0].set('defaultReward',rewards[0]);
						categories[0].save().then(function(){
							res.redirect("/admin/reward/index?category="+categories[0].id);
						});
					}
				});
			}
		});
	}
}
exports.updateForm = function (req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var query = new Parse.Query(Category);
		query.find({
			success:function(categories){
				var rewardId = req.query.id;
				var categoryId = req.query.categoryId;
				var query = new Parse.Query(Reward);
				query.equalTo("objectId", rewardId);
				query.find({
					success:function(reward){
						if(reward.length>0){
							res.render('reward/updateReward', {reward:reward[0],categories:categories,currentCategory:categoryId});
						}
						else{

						}
					}
				});
			}
		});
		
	}
}
exports.create = function (req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var name =req.body.name;
		var description = req.body.description;
		console.log(req.files);
		var file = req.body.file;
		console.log("file:"+JSON.stringify(file));
		var image = new Image();
		var imageFile;
		//var image = new Parse.File(file.name, file, "image/png");
		var query = new Parse.Query(Category);
		var reward = new Reward();
		reward.set('name',name);
		reward.set('description',description);
		reward.set('isDefault',req.body.isDefault);
		if(req.body.isDefault){

		}else{
			//reward.set('image',image);
			console.log("Category:"+req.body.category);
			query.equalTo("objectId",req.body.category);
			query.find({
				success:function(categories){
					console.log("Cat:"+JSON.stringify(categories));
					reward.set('category',categories[0]);
					if(file.indexOf("http")==0){
						Parse.Cloud.httpRequest({ url: file}).then(function(response) {	
							new Parse.File("rewardImage", {base64: response.buffer.toString('base64', 0, response.buffer.length)}).save().then(
								function(imageFile){
									reward.set('picture',imageFile);
									reward.save(null,{
										success:function(){
											res.redirect("/admin/reward/index");
										},
										error:function(error){
											console.log(error);
										}
									});
								}
								);
							console.log(JSON.stringify(imageFile));
						});
					}else{
						reward.save(null,{
							success:function(){
								res.redirect("/admin/reward/index");
							},
							error:function(error){
								console.log(error);
							}
						});
					}

				}
			});

		}
		
		
	}
}
exports.update = function (req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var query = new Parse.Query(Reward);
		console.log("req.body.rewardId:"+req.body.rewardId);
		query.equalTo("objectId",req.body.rewardId);
		query.find({
			success:function(rewards){
				if(rewards[0]!="undefined"){
					var reward = rewards[0];
					var name =req.body.name;
					var description = req.body.description;
					var file = req.body.file;
					console.log("file:"+JSON.stringify(file));
					var image = new Image();
					var imageFile;
					reward.set('isDefault',req.body.isDefault);
					if(req.body.isDefault){

					}else{
						//var image = new Parse.File(file.name, file, "image/png");
						var query = new Parse.Query(Category);
						reward.set('name',name);
						reward.set('description',description);

						//reward.set('image',image);
						console.log("Category ID:"+req.body.category);
						query.equalTo("objectId",req.body.category);
						query.find({
							success:function(categories){
								console.log("Cat:"+JSON.stringify(categories));
								reward.set('category',categories[0]);
								if(file.indexOf("http")==0){
									Parse.Cloud.httpRequest({ url: file}).then(function(response) {	
										new Parse.File("rewardImage", {base64: response.buffer.toString('base64', 0, response.buffer.length)}).save().then(
											function(imageFile){
												reward.set('picture',imageFile);
												reward.save(null,{
													success:function(){
														res.redirect("/admin/reward/index");
													},
													error:function(error){
														console.log(error);
													}
												});
											}
											);
										console.log(JSON.stringify(imageFile));
									});
								}else{
									reward.save(null,{
										success:function(){
											res.redirect("/admin/reward/index");
										},
										error:function(error){
											console.log(error);
										}
									});
								}
								
							}
						});
}
}else{
	res.redirect("/admin/reward/index");
}
}
})


}
}
exports.category = function (req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var query = new Parse.Query(Category);
		query.find({
			success:function(categories){
				console.log(JSON.stringify(categories));
				res.render('reward/categoryList', {categories:categories});
			}
		});
	}
}
exports.createCategoryForm = function (req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		res.render('reward/createCategory');
	}
}
exports.createCategory = function (req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var name = req.body.categoryName;
		var category = new Category();
		category.set("name",name);
		category.save(null,{
			success:function(){
				res.redirect("/admin/reward/categories");
			},
			error:function(error){
				console.log(error);
			}
		});
		
	}
}
exports.updateCategory = function (req, res) {
	console.log("update category");
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var i=1;
		console.log(JSON.stringify(req.body));
		var query = new Parse.Query(Category);
		var promises = [];
		query.find(function(categories){
			categories.forEach(function(category){
				if(req.body[category.id]!=null&&typeof req.body[category.id]!='undefined'){
					console.log(category.id+"    "+req.body[category.id])
					category.set('name',req.body[category.id]);
					promises.push(category.save());

				}
			});
		}).then(function(){
			Parse.Promise.when(promises).then(function(){
				res.redirect("/admin/reward/categories");
			});
		});
		
	}	
}


