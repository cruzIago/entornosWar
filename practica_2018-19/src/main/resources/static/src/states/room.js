Spacewar.roomState = function(game) {

}

Spacewar.roomState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **ROOM** state");
		}
	},

	preload : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Joining room...");
		}
		let message = {
			event : 'NEW GAME',
			kind : 'ROYALE'
		}
		game.global.socket.send(JSON.stringify(message))
	},

	create : function() {

	},

	update : function() {
		game.state.start('gameState')
	}
}