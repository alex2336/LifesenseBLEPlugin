var ACLUtil = require('cloud/util/acl.js').util;
var DB = require('cloud/domains/DB.js').DB;

exports.class = {
	reward: {
		dbType: {
			name: DB.DATATYPE.pointer,
			args:"RewardType"
		}
	},
	user: {
		dbType: {
			name: DB.DATATYPE.pointer,
			args:"_User"
		}
	},
	flag: {
		dbType: {
			name: DB.DATATYPE.boolean
		}
	},
	challenge: {
		dbType: {
			name: DB.DATATYPE.pointer,
			args: "Challenge"
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