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
	this.bNombre;
	this.bEnviar;
	
	//mensaje
	this.msg;
	this.aviso;
	
	// others
	this.tintAzul;
	this.tintRojo;
	this.tintNot;
	this.MAXSALAS;
	this.MAXCHARACTERS;
	this.posSalas;
	this.keyPress;
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
		MAXCHARACTERS = 10
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
		
		bNombre = game.add.button(526, 135, 'sala', nombreClick, this);
		bNombre.visible = false;
		bNombre.onInputOver.add(over, {button:bNombre});
		bNombre.onInputOut.add(out, {button:bNombre});
		let textNombre = game.add.text(70,43,'NombreSala',{font:"20px Arial",fill:"#ffffff"});
		bNombre.addChild(textNombre);
		
		bEnviar = game.add.button(526, 525, 'enviar', enviarClick, this);
		bEnviar.visible = false;
		bEnviar.onInputOver.add(over, {button:bEnviar});
		bEnviar.onInputOut.add(out, {button:bEnviar});
		
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
		
		//control escritura
		game.input.keyboard.addCallbacks(this, null, null, keyPressMenu);
		remove = game.input.keyboard.addKey(Phaser.Keyboard.BACKSPACE);
	    remove.onDown.add(removePressMenu, this);
	    game.input.keyboard.addKeyCapture([Phaser.Keyboard.BACKSPACE]);
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
		bNombre.visible = true
		bEnviar.visible = true
		bEmpezar.visible = true
		bModoClassic.visible = true
		bModoBattleRoyal.visible = true
	} else {
		bCrearSala.tint = tintAzul
		bEmpezar.tint = tintNot
		bNombre.visible = false
		bEnviar.visible = false
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

function chatClick() {
	if (bChat.tint === tintAzul) {
		bChat.tint = tintRojo
	} else {
		bChat.tint = tintAzul
	}
}

//gestion salas
function empezarClick() {
	if (bEmpezar.tint === tintAzul) {
		bEmpezar.tint = tintRojo
	} else {
		bEmpezar.tint = tintAzul
	}
}

function nombreClick() {
	if (bNombre.tint === tintAzul && bEnviar.tint !== tintRojo) {
		bNombre.tint = tintRojo
	} else {
		bNombre.tint = tintAzul
	}
}

function enviarClick() {
	if ((bModoBattleRoyal.tint === tintRojo || bModoClassic.tint === tintRojo) && bNombre.tint === tintNot) {
		if (bEnviar.tint === tintAzul) {
			msg.event = 'NEW SALA'
			msg.creador = game.global.nombreJugador
			msg.nombre = bNombre.getChildAt(0).text.toString()
			game.global.socket.send(JSON.stringify(msg))
			bEnviar.tint = tintRojo
		} else {
			msg.event = 'CANCEL SALA'
			msg.creador = game.global.nombreJugador
			game.global.socket.send(JSON.stringify(msg))
			bEnviar.tint = tintAzul
		}
	}
}

function modoClassicClick() {
	if (bModoBattleRoyal.tint === tintNot && bEnviar.tint === tintNot) {
		if (bModoClassic.tint === tintAzul) {
			msg.modo = 'classic'
			msg.njugadores = 2
			bModoClassic.tint = tintRojo
		} else {
			bModoClassic.tint = tintAzul
		}
	}
}

function modoBattleRoyalClick() {
	if (bModoClassic.tint === tintNot && bEnviar.tint === tintNot) {
		if (bModoBattleRoyal.tint === tintAzul) {
			msg.modo = 'battleRoyal'
			msg.njugadores = 10
			bModoBattleRoyal.tint = tintRojo
		} else {
			bModoBattleRoyal.tint = tintAzul
		}
	}
}

function salaClick() {
	if (this.button.tint === tintAzul && this.button.getChildAt(0).text !== '') {
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

//funciones de escritura
this.keyPressMenu = function(char) {
	if (bNombre.tint === tintRojo && bNombre.getChildAt(0).text.length <= MAXCHARACTERS) {
		bNombre.getChildAt(0).text += char
	}
}

this.removePressMenu = function() {
	if (bNombre.tint === tintRojo) {
		let str = bNombre.getChildAt(0).text.toString()
		bNombre.getChildAt(0).text = str.slice(0, str.length-1)
	}
}
