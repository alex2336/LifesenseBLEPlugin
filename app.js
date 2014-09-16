var express = require('express'),
	moment = require('moment'),
	_ = require('underscore'),
	parseExpressCookieSession = require('parse-express-cookie-session'),
	parseExpressHttpsRedirect = require('parse-express-https-redirect'),
	app = express();

var auth = require('cloud/modules/auth.js'),
	user = require('cloud/modules/user.js'),
	sync = require('cloud/modules/sync.js'),
	company = require('cloud/modules/company.js'),
	challenge = require('cloud/modules/challenge.js'),
	reward = require('cloud/modules/reward.js'),
	displayImage = require('cloud/modules/displayImage.js');
	system = require('cloud/internal-dev/system.js');	//this is only for internal developers

var urlPrefix = "/admin";

app.use(parseExpressHttpsRedirect());  // Require user to be on HTTPS.

app.use(express.bodyParser());// Middleware for reading request body

app.use(express.cookieParser('YOUR_SIGNING_SECRET'));
app.use(parseExpressCookieSession({ cookie: { maxAge: 3600000 } }));

app.set('views', 'cloud/views');// Specify the folder to find templates
app.set('view engine', 'ejs');

app.locals.formatDate = function(date){
	return moment(date).format('MM/DD/YYYY');
}
app.locals.addDays = function(date, days) {
	return date.setDate(date.getDate() + days);
}

app

	.get(urlPrefix + "/index", auth.index)

	/**
	 * Admin user signin
	 * @param {Object} params Login credentials
	 */
	.post(urlPrefix + "/signin", auth.signin)
	.get(urlPrefix + "/signinlinkedin", auth.signinLinkedin)
	.get(urlPrefix + "/signinlinkedinfrombrowser", auth.signinLinkedinFromBrowser)
	.get(urlPrefix + "/connectRunkeeper", auth.connectRunkeeper)
	.get(urlPrefix + "/disconnectRunkeeper", auth.disconnectRunkeeper)
	/**
	 * Admin user signout
	 * @param {Null}
	 */
	.get(urlPrefix + "/signout", auth.signout)

	/**
	* show parse image as cloud code
	*/
	.get("/displayImage", displayImage.show)
	/**
	 * List all users
	 * @param {Null}
	 */
	.get(urlPrefix + "/users", user.list)

	.get(urlPrefix + "/companies", company.list)
	//.get(urlPrefix + "/company/admin", company.list)
	.get(urlPrefix + "/company/create", company.form)
	.post(urlPrefix + "/company", company.create)
	// .get(urlPrefix + "/company/:id", company.get)
	// .put(urlPrefix + "/company/:id", company.update)
	// .delete(urlPrefix + "/company/:id", company.delete)

	.get(urlPrefix + "/challenge/new/:id", challenge.form)
	.get(urlPrefix + "/challenge/edit/:id", challenge.editform)
	.post(urlPrefix + "/challenge/Create", challenge.create)
	.post(urlPrefix + "/challenge/Update", challenge.update)

	/*
	*Rewards related
	*/
	.get(urlPrefix + "/reward/index", reward.index)
	.post(urlPrefix + "/reward/create", reward.create)
	.get(urlPrefix + "/reward/create", reward.createForm)
	.get(urlPrefix + "/reward/update", reward.updateForm)
	.get(urlPrefix + "/reward/categories", reward.category)
	.get(urlPrefix + "/reward/category/create", reward.createCategoryForm)
	.post(urlPrefix + "/reward/category/create", reward.createCategory)
	/**
	 * Sync data from external services
	 */
	.get(urlPrefix + "/syncRunkeeper", sync.importRunkeeper)

	/**
	 * System settings for internal developers
	 */
	.get("/internal-dev/system", system.home)
	.get("/internal-dev/system/bootStrap", system.bootStrap)
	.get("/internal-dev/system/createTables", system.createTables)
	.get("/internal-dev/system/populateData", system.populateData)
	.get("/internal-dev/system/deleteSurvey", system.deleteSurvey)
	.get("/internal-dev/system/populateSurvey", system.populateSurvey)
	.get("/internal-dev/system/populateSurvey2", system.populateSurvey2)
	.get("/internal-dev/system/linkTrekLocations", system.linkTrekLocations)
	.get("/internal-dev/system/sendWeeklyTrivia", system.sendWeeklyTrivia)
	.get("/internal-dev/system/updateRelations", system.updateRelations)
	.get("/internal-dev/system/deleteAllRows", system.deleteAllRowsFromTables)
	.get("/internal-dev/system/populateUserStatistics", system.populateUserStatistics)
	.get("/internal-dev/system/populateTeamStatistics", system.populateTeamStatistics)
	.post("/internal-dev/system/populateDemoData", system.populateDemoData)
	.get("/internal-dev/system/daily", system.daily)

	.get("/internal-dev/system/sendPostSurveyNotification", system.sendPostSurveyNotification)
	.get("/internal-dev/system/sendStrictPostSurveyNotification", system.sendStrictPostSurveyNotification)

	;

app.listen();