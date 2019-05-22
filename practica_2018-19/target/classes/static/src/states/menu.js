Spacewar.menuState = function(game) {
	// sprites
	this.fondo;
	this.buscando;
	this.salonFama;
	
	// buttons
	this.bCrearSala;
	this.bEmpezar;
	this.bMatchmaking;
	this.bChat;
	this.bModoBattleRoyal;
	this.bModoClassic;
	this.bSalas;
	
	//mensaje
	this.msg;
	this.aviso;
	
	// others
	this.tintAzul;
	this.tintRojo;
	this.tintNot;
	this.MAXSALAS;
	this.posSalas;
}

Spacewar.menuState.prototype = {

	init : function() {
		game.global.response=false
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
		tintAzul = 0x5f92fe
		tintRojo = 0xff3d3d
		tintNot = 0xffffff
		bSalas = []
		msg = new Object()
		MAXSALAS = 10
		posSalas = [[392, 135], [660, 135], [392, 220], [660, 220], [392, 305], [660, 305], [392, 390], [660, 390], [392, 475], [660, 475]]
		game.global.updateMenu=function(){
			var text;
			for(var i=0;i<bSalas.length;i++){
				if(game.global.salas.length>i && typeof game.global.salas!=='undefined'){
					bSalas[i].getChildAt(0).text = game.global.salas[i].nombre;
					bSalas[i].getChildAt(0).visible = true;
				}
				else{
					//bSalas[i].getChildAt(0).visible = false;
				}
			}
		}
	},

	preload : function() {
		// sprites
		fondo = game.add.sprite(0, 0, 'fondoConLogo')
		buscando = game.add.sprite(0, 610, 'buscando')
		buscando.visible = false
		salonFama = game.add.sprite(896, 100, 'salonFama_Chat')
	},

	create : function() {
		let mensaje=new Object()
		mensaje.event="ADD PLAYER"
		game.global.socket.send(JSON.stringify(mensaje))
					
		// buttons
		bCrearSala = game.add.button(0, 610, 'crearSala', crearSalaClick, this);
		bCrearSala.onInputOver.add(over, {button:bCrearSala});
		bCrearSala.onInputOut.add(out, {button:bCrearSala});
		
		bMatchmaking = game.add.button(696, 610, 'matchmaking', matchmakingClick, this);
		bMatchmaking.onInputOver.add(over, {button:bMatchmaking});
		bMatchmaking.onInputOut.add(out, {button:bMatchmaking});
		
		bEmpezar = game.add.button(696, 610, 'empezar', empezarClick, this);
		bEmpezar.visible = false;
		bEmpezar.onInputOver.add(over, {button:bEmpezar});
		bEmpezar.onInputOut.add(out, {button:bEmpezar});
		
		bChat = game.add.button(0, 100, 'salonFama_Chat', chatClick, this);
		bChat.onInputOver.add(over, {button:bChat});
		bChat.onInputOut.add(out, {button:bChat});
		
		bModoClassic = game.add.button(392, 220, 'salaClassic', modoClassicClick, this);
		bModoClassic.visible = false;
		bModoClassic.onInputOver.add(over, {button:bModoClassic});
		bModoClassic.onInputOut.add(out, {button:bModoClassic});
		
		bModoBattleRoyal = game.add.button(660, 220, 'salaBattleRoyal', modoBattleRoyalClick, this);
		bModoBattleRoyal.visible = false;
		bModoBattleRoyal.onInputOver.add(over, {button:bModoBattleRoyal});
		bModoBattleRoyal.onInputOut.add(out, {button:bModoBattleRoyal});
		
		for (var i = 0; i < MAXSALAS; i++) {
			bSalas[i] = game.add.button(posSalas[i][0], posSalas[i][1], 'sala');
			bSalas[i].onInputDown.add(salaClick, {button:bSalas[i]});
			bSalas[i].onInputOver.add(over, {button:bSalas[i]});
			bSalas[i].onInputOut.add(out, {button:bSalas[i]});
			let text = game.add.text(0,0,'',{font:"16px Arial",fill:"#ffffff"});
			text.visible = false;
			bSalas[i].addChild(text);
		}
		
		//Avisos
		aviso = game.add.text(640,360,"SALAS LLENAS",{font:"30px Arial",fill:"#ff3d3d"});
		aviso.visible = false;
	},
	
	update: function() {
		//modificar texto y hacer visibles solo las que esten disponibles
		/*if (bCrearSala.tint === tintNot) {
			for (var i = 0; i < game.global.salas.length; i++) {
				bSalas[i].visible = true
				text[i] = game.global.salas[i]
				text[i].visible = true
			}
		}*/
		
		if(game.global.response){
			aviso.visible=true;
			setTimeout(function(){
				aviso.visible=false;
				game.global.response=false;
			},3000);
		}
	}
}

function over() {
	if (this.button.tint === tintNot) {
		this.button.tint = tintAzul
	}
}

function out() {
	if (this.button.tint === tintAzul) {
		this.button.tint = tintNot
	}
}

function crearSalaClick() {
	if (bCrearSala.tint === tintAzul) {	
		bCrearSala.tint = tintRojo
		for (var bSala of bSalas) {
			bSala.visible = false
		}
		bMatchmaking.visible = false
		bEmpezar.visible = true
		bModoClassic.visible = true
		bModoBattleRoyal.visible = true
	} else {
		bCrearSala.tint = tintAzul
		bEmpezar.tint = tintNot
		bEmpezar.visible = false
		bModoClassic.visible = false
		bModoBattleRoyal.visible = false
		bMatchmaking.visible = true
		for (var bSala of bSalas) {
			bSala.visible = true
		}
	}
}

function matchmakingClick() {
	if (bMatchmaking.tint === tintAzul) {
		bMatchmaking.tint = tintRojo
		bCrearSala.visible = false
		buscando.visible = true
	} else {
		bMatchmaking.tint = tintAzul
		buscando.visible = false
		bCrearSala.visible = true
	}
}

function empezarClick() {
	if (bEmpezar.tint === tintAzul) {
		bEmpezar.tint = tintRojo
	} else {
		bEmpezar.tint = tintAzul
	}
}

function chatClick() {
	if (bChat.tint === tintAzul) {
		bChat.tint = tintRojo
	} else {
		bChat.tint = tintAzul
	}
}

function modoClassicClick() {
	if (bModoBattleRoyal.tint === tintNot) {
		if (bModoClassic.tint === tintAzul) {
			msg.event = 'NEW SALA'
			msg.modo = 'classic'
			msg.njugadores = 2
			msg.nombre = 'un0o'
			game.global.socket.send(JSON.stringify(msg))
			bModoClassic.tint = tintRojo
		} else {
			msg.event = 'CANCEL SALA'
			game.global.socket.send(JSON.stringify(msg))
			bModoClassic.tint = tintAzul
		}
	}
}

function modoBattleRoyalClick() {
	if (bModoClassic.tint === tintNot) {
		if (bModoBattleRoyal.tint === tintAzul) {
			msg.event = 'NEW SALA'
			msg.modo = 'battleRoyal'
			msg.njugadores = 10
			msg.nombre = 'battleun0o'
			game.global.socket.send(JSON.stringify(msg))
			bModoBattleRoyal.tint = tintRojo
		} else {
			msg.event = 'CANCEL SALA'
			game.global.socket.send(JSON.stringify(msg))
			bModoBattleRoyal.tint = tintAzul
		}
	}
}

function salaClick() {
	if (this.button.tint === tintAzul) {
		for (var bSala of bSalas) {
			bSala.visible = false
		}
		this.button.visible = true
		this.button.tint = tintRojo
	} else {
		this.button.tint = tintAzul
		for (var bSala of bSalas) {
			bSala.visible = true
		}
	}
}