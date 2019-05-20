Spacewar.menuState = function(game) {
	this.fondo;
	this.buscando;
	this.cancelar;
	this.matchmaking;
	this.sala;
	this.salaSeleccionada;
	this.salonFama_Chat;	
}

Spacewar.menuState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		fondo = game.add.sprite(0, 0, 'fondoConLogo')
		matchmaking = game.add.sprite(356, 610, 'matchmaking')
		sala = game.add.sprite(392, 220, 'sala')
		salonFama = game.add.sprite(896, 100, 'salonFama_Chat')
		chat = game.add.sprite(10, 100, 'salonFama_Chat')
	},

	create : function() {
	    
	}/*,
	
	update: function() {
		if (text.text.length >= 10) {
			console.log("Te pasas")
		}
	}*/
}