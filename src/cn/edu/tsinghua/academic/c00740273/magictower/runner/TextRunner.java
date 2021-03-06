package cn.edu.tsinghua.academic.c00740273.magictower.runner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONTokener;

import cn.edu.tsinghua.academic.c00740273.magictower.engine.Coordinate;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.DataException;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.Engine;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.Event;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.GameTerminationException;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardEvent;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardGame;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardGameFailureTerminationException;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardGameSuccessTerminationException;

public class TextRunner {

	protected String data;
	protected Map<String, byte[]> storage;
	protected Scanner scanner;

	public TextRunner(String data) {
		this.data = data;
		this.storage = new HashMap<String, byte[]>();
		this.scanner = new Scanner(System.in);
	}

	public static void main(String[] args) throws IOException, DataException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, JSONException {
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
		textRunner.run();
	}

	public void run() throws DataException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, JSONException {
		Engine engine = new Engine();
		StandardGame game = new StandardGame(this.data);
		TextRenderer renderer = (TextRenderer) game.getRenderer();
		StandardEvent event = (StandardEvent) engine.loadGame(game);

		while (true) {
			renderer.renderRoundEvent(engine, event);
			try {
				event = (StandardEvent) this.runCommand(engine, renderer);
			} catch (StandardGameSuccessTerminationException e) {
				System.out.print("Success! " + e.getMessage()
						+ " / AttrCheck: "
						+ e.getAttributeCheck().getAttributeName() + " / ");
				renderer.renderEvent((StandardEvent) e.getEvent());
				return;
			} catch (StandardGameFailureTerminationException e) {
				System.out.print("Failed. " + e.getMessage() + " / AttrCheck: "
						+ e.getAttributeCheck().getAttributeName() + " / ");
				renderer.renderEvent((StandardEvent) e.getEvent());
				return;
			} catch (GameTerminationException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public Event runCommand(Engine engine, TextRenderer renderer)
			throws GameTerminationException, IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, JSONException {
		while (true) {
			System.out.print("> ");
			String cmd = this.scanner.next();
			if (cmd.matches("[msa]")) {
				int z = this.scanner.nextInt();
				int x = this.scanner.nextInt();
				int y = this.scanner.nextInt();
				Coordinate coord = new Coordinate(z, x, y);
				Event event = null;
				if (cmd.equals("m")) {
					event = engine.moveTo(coord);
				} else if (cmd.equals("s")) {
					event = engine.simulateMoveTo(coord);
				} else if (cmd.equals("a")) {
					event = engine.attemptMoveTo(coord);
				}
				return event;
			} else if (cmd.matches("[hjkl]+")) {
				Event event = null;
				for (int i = 0; i < cmd.length(); i++) {
					Coordinate currentCoord = engine.getCurrentCoordinate();
					int z = currentCoord.getZ();
					int x = currentCoord.getX();
					int y = currentCoord.getY();
					if (cmd.charAt(i) == 'h') {
						y--;
					} else if (cmd.charAt(i) == 'l') {
						y++;
					} else if (cmd.charAt(i) == 'j') {
						x++;
					} else if (cmd.charAt(i) == 'k') {
						x--;
					}
					Coordinate coord = new Coordinate(z, x, y);
					event = engine.moveTo(coord);
				}
				return event;
			} else if (cmd.equals("w")) {
				String storageKey = this.scanner.next();
				byte[] data = engine.serializeGame();
				this.storage.put(storageKey, data);
				System.out.println(data.length + " bytes saved as "
						+ storageKey + ".");
			} else if (cmd.equals("r")) {
				String storageKey = this.scanner.next();
				byte[] data = this.storage.get(storageKey);
				if (data == null) {
					System.out.println(storageKey + " not found in storage.");
				} else {
					engine.unserializeGame(data);
					return new StandardEvent(engine.getCurrentCoordinate());
				}
			} else if (cmd.equals("t")) {
				String terminationStr = this.scanner.next();
				Engine.Termination termination = null;
				if (terminationStr.equals("a")) {
					termination = Engine.Termination.AUTOMATIC;
				} else if (terminationStr.equals("m")) {
					termination = Engine.Termination.MANUAL;
				} else if (terminationStr.equals("n")) {
					termination = Engine.Termination.NEVER;
				} else {
					System.out.println("Invalid termination mode.");
				}
				if (termination != null) {
					String targetStr = this.scanner.next();
					for (char target : targetStr.toCharArray()) {
						switch (target) {
						case 's':
							engine.setSuccessTermination(termination);
							break;
						case 'f':
							engine.setFailureTermination(termination);
							break;
						default:
							System.out.println("Invalid target: " + target);
							break;
						}
					}
					System.out.println("Termination mode set.");
				}
			} else if (cmd.equals("v")) {
				String attributeKey = this.scanner.next();
				String attributeValueStr = this.scanner.next();
				JSONTokener tokener = new JSONTokener(attributeValueStr);
				Object attributeValue = tokener.nextValue();
				engine.setAttribute(attributeKey, attributeValue);
				return new StandardEvent(engine.getCurrentCoordinate());
			} else if (cmd.equals("z")) {
				return new StandardEvent(engine.getCurrentCoordinate());
			} else {
				System.out.println("Invalid command.");
			}
		}
	}
}
