var ACLUtil = require('cloud/util/acl.js').util;
var DB = require('cloud/domains/DB.js').DB;

exports.class = {
	name: {
		dbType: {
			name: DB.DATATYPE.string
		}
	},
	acl: ACLUtil.PUBLIC,
	beforeSave: function(request, response) {
		// Add custom beforeSave implementation if required
	},
	afterSave: function(request, response) {
		// Add custom afterSave implementation if required
	}
}