var Category = Parse.Object.extend("RewardCategory");
var Reward = Parse.Object.extend("Reward");


exports.index = function onIndex(req, res) {
	var currentUser = Parse.User.current();
	
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var query = new Parse.Query(Category);
		query.find({
			success:function(categories){
				var query = new Parse.Query(Reward);
				query.find({
					success:function(rewards){
						rewards.forEach(function(reward){
							for(var i=0;i<categories.length;i++){
								if(categories[i].objectId == reward.get('category').objectId){
									console.log("cat3:"+JSON.stringify(reward.get('category').objectId));
									console.log("cat2:"+JSON.stringify(categories[i]));
									reward.categoryId = categories[i].get('categoryId');
									reward.objectId = reward.get('objectId');
									break;
								}
							}
						});
						console.log(rewards)
						console.log(JSON.stringify(rewards));
						res.render('reward/list', {rewards:rewards});
					}
				});
			}
		});
		
	}
}
exports.createForm = function onIndex(req, res) {
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
exports.updateForm = function onIndex(req, res) {
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
							res.render('reward/updateReward', {reward:reward[0],categories:categories});
						}
						else{

						}
					}
				});
			}
		});
		
	}
}
exports.create = function onIndex(req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var name =req.body.name;
		var description = req.body.description;
		var file = req.body.file;
		console.log("file:"+JSON.stringify(file));
		//var image = new Parse.File(file.name, file, "image/png");
		var query = new Parse.Query(Category);
		var reward = new Reward();
		reward.set('name',name);
		reward.set('description',description);
		//reward.set('image',image);
		console.log("Category ID:"+req.body.category);
		query.equalTo("categoryId",parseInt(req.body.category));
		query.find({
			success:function(categories){
				console.log("Cat:"+JSON.stringify(categories));
				reward.set('category',categories[0]);
				reward.save(null,{
					success:function(){
						res.redirect("/admin/reward/index");
					},
					error:function(error){
						console.log(error);
					}
				});
			}
		});
	}
}
exports.category = function onIndex(req, res) {
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
exports.createCategoryForm = function onIndex(req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		res.render('reward/createCategory');
	}
}
exports.createCategory = function onIndex(req, res) {
	var currentUser = Parse.User.current();
	if (!currentUser) {
		res.render('login', {errorMessage: "You are not logged-in."});
	} else {
		var price = req.body.price;
		console.log(price);
		var query = new Parse.Query(Category);
		query.count({
			success:function(count){
				console.log(count);
				var category = new Category();
				category.set("categoryId",parseInt(count+1));
				category.set("price",parseFloat(price));
				category.save(null,{
					success:function(){
						res.redirect("/admin/reward/categories");
					},
					error:function(error){
						console.log(error);
					}
				});
			},
			error:function(msg){
				console.log(msg);
			}
		});
	}
}

