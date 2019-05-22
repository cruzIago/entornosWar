package spacewar;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

public class SalaObject {
	private CyclicBarrier nPlayers;
	private CopyOnWriteArrayList<Player> playersSala;
	private Runnable ePartida = ()->empezarPartida();
	private final String MODOJUEGO;
	private final String NOMBRE;
	
	public SalaObject(int NJUGADORES, String MODOJUEGO, String NOMBRE) {
		this.nPlayers = new CyclicBarrier(NJUGADORES, ePartida);
		this.MODOJUEGO = MODOJUEGO;
		this.playersSala = new CopyOnWriteArrayList<Player>();
		this.NOMBRE = NOMBRE;
	}
	
	private void empezarPartida() {
		
	}
	
	public String getModoJuego() {
		return MODOJUEGO;
	}
	
	public synchronized int getNumberPlayersWaiting() {
		return nPlayers.getNumberWaiting();
	}
	
	public void joinSala(Player player) {
		try {
			playersSala.add(player);
			nPlayers.await();
		} catch (InterruptedException e) {
			playersSala.remove(player);
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
	}
	
	public CopyOnWriteArrayList<Player> getPlayers() {
		return playersSala;
	}
	
	public String getNombreSala() {
		return NOMBRE;
	}
}
