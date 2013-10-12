package cn.edu.tsinghua.academic.c00740273.magictower.runner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import cn.edu.tsinghua.academic.c00740273.magictower.engine.Coordinate;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.DataException;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.Engine;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.Event;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.GameFailureTerminationException;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.GameSuccessTerminationException;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.GameTerminationException;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardEvent;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardGame;

public class TextRunner {

	protected String data;
	protected Map<String, byte[]> storage;

	public TextRunner(String data) {
		this.data = data;
		this.storage = new HashMap<String, byte[]>();
	}

	public static void main(String[] args) throws IOException, DataException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (args.length < 1) {
			System.err.println("Usage: java TextRunner data_file");
			System.exit(1);
		}

		StringBuilder dataBuilder = new StringBuilder();
		String dataLine;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0]), "utf-8"));
		while ((dataLine = reader.readLine()) != null) {
			dataBuilder.append(dataLine);
		}
		reader.close();
		String data = dataBuilder.toString();

		TextRunner textRunner = new TextRunner(data);
		try {
			textRunner.run();
		} catch (GameSuccessTerminationException e) {
			System.out.println("Success!");
		} catch (GameFailureTerminationException e) {
			System.out.println("Failed.");
		} catch (GameTerminationException e) {
			e.printStackTrace();
		}
	}

	public void run() throws DataException, GameTerminationException,
			IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Engine engine = new Engine();
		StandardGame game = new StandardGame(this.data);
		TextRenderer renderer = (TextRenderer) game.getRenderer();
		StandardEvent event = (StandardEvent) engine.loadGame(game);

		while (true) {
			renderer.renderRoundEvent(engine, event);
			event = (StandardEvent) this.runCommand(engine, renderer);
		}
	}

	@SuppressWarnings("resource")
	public Event runCommand(Engine engine, TextRenderer renderer)
			throws GameTerminationException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		while (true) {
			System.out.print("> ");
			Scanner scanner = new Scanner(System.in);
			String cmd = scanner.next();
			if (cmd.equals("m")) {
				int z = scanner.nextInt();
				int x = scanner.nextInt();
				int y = scanner.nextInt();
				scanner.nextLine();
				Coordinate coord = new Coordinate(z, x, y);
				return engine.moveTo(coord, null);
			} else if (cmd.equals("s")) {
				int z = scanner.nextInt();
				int x = scanner.nextInt();
				int y = scanner.nextInt();
				scanner.nextLine();
				Coordinate coord = new Coordinate(z, x, y);
				return engine.simulateMoveTo(coord, null);
			} else if (cmd.equals("a")) {
				int z = scanner.nextInt();
				int x = scanner.nextInt();
				int y = scanner.nextInt();
				scanner.nextLine();
				Coordinate coord = new Coordinate(z, x, y);
				return engine.attemptMoveTo(coord, null);
			} else if (cmd.equals("h")) {
				scanner.nextLine();
				Coordinate currentCoord = engine.getCurrentCoordinate();
				int z = currentCoord.getZ();
				int x = currentCoord.getX();
				int y = currentCoord.getY() - 1;
				Coordinate coord = new Coordinate(z, x, y);
				return engine.moveTo(coord, null);
			} else if (cmd.equals("l")) {
				scanner.nextLine();
				Coordinate currentCoord = engine.getCurrentCoordinate();
				int z = currentCoord.getZ();
				int x = currentCoord.getX();
				int y = currentCoord.getY() + 1;
				Coordinate coord = new Coordinate(z, x, y);
				return engine.moveTo(coord, null);
			} else if (cmd.equals("j")) {
				scanner.nextLine();
				Coordinate currentCoord = engine.getCurrentCoordinate();
				int z = currentCoord.getZ();
				int x = currentCoord.getX() + 1;
				int y = currentCoord.getY();
				Coordinate coord = new Coordinate(z, x, y);
				return engine.moveTo(coord, null);
			} else if (cmd.equals("k")) {
				scanner.nextLine();
				Coordinate currentCoord = engine.getCurrentCoordinate();
				int z = currentCoord.getZ();
				int x = currentCoord.getX() - 1;
				int y = currentCoord.getY();
				Coordinate coord = new Coordinate(z, x, y);
				return engine.moveTo(coord, null);
			} else if (cmd.equals("w")) {
				String storageKey = scanner.next();
				scanner.nextLine();
				byte[] data = engine.serializeGame();
				this.storage.put(storageKey, data);
				System.out.println("Saved as " + storageKey + ".");
			} else if (cmd.equals("r")) {
				String storageKey = scanner.next();
				scanner.nextLine();
				byte[] data = this.storage.get(storageKey);
				if (data == null) {
					System.out.println(storageKey + " not found in storage.");
				} else {
					engine.unserializeGame(data);
					return new StandardEvent(engine.getCurrentCoordinate());
				}
			} else {
				System.out.println("Invalid command.");
			}
		}
	}
}
