Spacewar.preloadState = function(game) {

}

Spacewar.preloadState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **PRELOAD** state");
		}
	},

	preload : function() {
		game.load.atlas('spacewar', 'assets/atlas/spacewar.png',
				'assets/atlas/spacewar.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.atlas('explosion', 'assets/atlas/explosion.png',
				'assets/atlas/explosion.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.image('avisoUso', 'assets/images/AvisoEnUso.png')
		game.load.image('avisoMaxCaracter', 'assets/images/AvisoMaxCaracteres.png')
		game.load.image('buscando', 'assets/images/buscando.png')
		game.load.image('cancelar', 'assets/images/cancelar.png')
		game.load.image('fondoConLogo', 'assets/images/fondoConLogo.png')
		game.load.image('login', 'assets/images/login.png')
		game.load.image('matchmaking', 'assets/images/matchmaking.png')
		game.load.image('sala', 'assets/images/sala.png')
		game.load.image('salaSeleccionada', 'assets/images/salaSeleccionada.png')
		game.load.image('salonFama_Chat', 'assets/images/salonFama_Chat.png')
	},

	create : function() {
		game.state.start('matchmakingState')
		//game.state.start('loginState')
	},

	update : function() {

	}
}