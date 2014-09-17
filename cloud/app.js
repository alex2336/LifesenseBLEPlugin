var express = require('express');
var parseExpressHttpsRedirect = require('parse-express-https-redirect');
var parseExpressCookieSession = require('parse-express-cookie-session');
var app = express();
var reward = require('cloud/reward.js');
var urlPrefix = "/admin";
app.set('views', 'cloud/views');
app.set('view engine', 'ejs');
  app.use(parseExpressHttpsRedirect());  // Require user to be on HTTPS.
  app.use(express.bodyParser());
  app.use(express.cookieParser('YOUR_SIGNING_SECRET'));
  app.use(parseExpressCookieSession({ cookie: { maxAge: 3600000 } }));

  // You could have a "Log In" link on your website pointing to this.
  app.get('/login', function(req, res) {
    // Renders the login form asking for username and password.
    res.render('login.ejs');
  });

  app.get('/create', function(req, res) {
    // Renders the login form asking for username and password.
    Parse.User.signUp('admin','gongjianlin');
  });
  // Clicking submit on the login form triggers this.
  app.post('/login', function(req, res) {
    Parse.User.logIn(req.body.username, req.body.password).then(function() {
      // Login succeeded, redirect to homepage.
      // parseExpressCookieSession will automatically set cookie.
      res.redirect('/');
    },
    function(error) {
      // Login failed, redirect back to login form.
      res.redirect('/login');
    });
  });

  // You could have a "Log Out" link on your website pointing to this.
  app.get('/logout', function(req, res) {
    Parse.User.logOut();
    res.redirect('/');
  })  
  .get(urlPrefix + "/reward/index", reward.index)
  .post(urlPrefix + "/reward/create", reward.create)
  .get(urlPrefix + "/reward/create", reward.createForm)
  .get(urlPrefix + "/reward/update", reward.updateForm)
  .post(urlPrefix + "/reward/update", reward.update)
  .get(urlPrefix + "/reward/categories", reward.category)
  .get(urlPrefix + "/reward/category/create", reward.createCategoryForm)
  .post(urlPrefix + "/reward/category/create", reward.createCategory)
  .post(urlPrefix + "/reward/category/update", reward.updateCategory)
  .get(urlPrefix + "/reward/setDefault", reward.setDefault)
  // The homepage renders differently depending on whether user is logged in.
  app.get('/', function(req, res) {
    if (Parse.User.current()) {
      // No need to fetch the current user for querying Note objects.
      var Note = Parse.Object.extend("Note");
      var query = new Parse.Query(Note);
      query.find().then(function(results) {
        // Render the notes that the current user is allowed to see.
      },
      function(error) {
        // Render error page.
      });
    } else {
      // Render a public welcome page, with a link to the '/login' endpoint.
    }
  });

  // You could have a "Profile" link on your website pointing to this.
  app.get('/profile', function(req, res) {
    // Display the user profile if user is logged in.
    if (Parse.User.current()) {
      // We need to fetch because we need to show fields on the user object.
      Parse.User.current().fetch().then(function(user) {
        // Render the user profile information (e.g. email, phone, etc).
      },
      function(error) {
        // Render error page.
      });
    } else {
      // User not logged in, redirect to login form.
      res.redirect('/login');
    }
  });

  app.listen();