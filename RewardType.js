var ACLUtil = require('cloud/util/acl.js').util;
var DB = require('cloud/domains/DB.js').DB;

exports.class = {
	name: {
		dbType: {
			name: DB.DATATYPE.string
		}
	},
	description: {
		dbType: {
			name: DB.DATATYPE.string
		}
	},
	picture: {
		dbType: {
			name: DB.DATATYPE.file
		}
	},
	category: {
		dbType: {
			name: DB.DATATYPE.pointer,
			args: "RewardCategory"
		}
	},
	isDefault: {
		dbType: {
			name: DB.DATATYPE.boolean
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