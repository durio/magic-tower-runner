package cn.edu.tsinghua.academic.c00740273.magictower.runner;

import java.io.Console;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.tsinghua.academic.c00740273.magictower.engine.Coordinate;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.Engine;
import cn.edu.tsinghua.academic.c00740273.magictower.engine.Tile;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.DataFormatException;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardEvent;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardRenderer;
import cn.edu.tsinghua.academic.c00740273.magictower.standard.StandardTile;

public class TextRenderer implements StandardRenderer {

	protected static Map<String, String> ansiCodes;

	protected int columnWidth;
	protected String characterText;
	protected boolean isConsole;
	protected PrintWriter writer;

	static {
		ansiCodes = new HashMap<String, String>();
		ansiCodes.put("foreground-black", "30m");
		ansiCodes.put("foreground-red", "31m");
		ansiCodes.put("foreground-green", "32m");
		ansiCodes.put("foreground-yellow", "33m");
		ansiCodes.put("foreground-blue", "34m");
		ansiCodes.put("foreground-magenta", "35m");
		ansiCodes.put("foreground-cyan", "36m");
		ansiCodes.put("foreground-white", "37m");
		ansiCodes.put("background-black", "40m");
		ansiCodes.put("background-red", "41m");
		ansiCodes.put("background-green", "42m");
		ansiCodes.put("background-yellow", "43m");
		ansiCodes.put("background-blue", "44m");
		ansiCodes.put("background-magenta", "45m");
		ansiCodes.put("background-cyan", "46m");
		ansiCodes.put("background-white", "47m");
	}

	public TextRenderer() {
		Console console = System.console();
		if (console != null) {
			this.writer = console.writer();
			this.isConsole = true;
		} else {
			this.writer = new PrintWriter(System.out, true);
			this.isConsole = false;
		}
	}

	@Override
	public void initialize(JSONObject dataValue) throws JSONException,
			DataFormatException {
		this.columnWidth = dataValue.getInt("column-width");
		this.characterText = dataValue.getString("character-text");
	}

	public void renderRoundEvent(Engine engine, StandardEvent event) {
		if (this.isConsole) {
			this.writer.println("\u001b[2J\u001b[H");
		}
		this.renderRound(engine);
		this.renderEvent(event);
	}

	public void renderRound(Engine engine) {
		this.renderRoundHeader(engine);
		this.renderRoundTiles(engine);
		this.renderRoundAttributes(engine);
	}

	public void renderRoundHeader(Engine engine) {
		Coordinate currentCoord = engine.getCurrentCoordinate();
		Coordinate maxCoord = engine.getMaximumCoordinate();
		this.writer.format("CUR (%d, %d, %d) MAX (%d, %d, %d) =[%d]=\n\n",
				currentCoord.getZ(), currentCoord.getX(), currentCoord.getY(),
				maxCoord.getZ(), maxCoord.getX(), maxCoord.getY(),
				currentCoord.getZ());
	}

	public void renderRoundTiles(Engine engine) {
		// First line of the grid.
		this.outputColumn("x\\y");
		Coordinate currentCoord = engine.getCurrentCoordinate();
		Coordinate maxCoord = engine.getMaximumCoordinate();
		for (int i = 0; i <= maxCoord.getY(); i++) {
			this.outputColumn(String.valueOf(i));
		}
		this.writer.println();
		this.writer.println();

		// The rest part.
		Tile[][] tiles = engine.getLayerTiles(currentCoord.getZ());
		for (int i = 0; i <= maxCoord.getX(); i++) {
			this.outputColumn(String.valueOf(i));
			StandardTile[] tileRow = (StandardTile[]) tiles[i];
			for (StandardTile tile : tileRow) {
				Map<String, Object> renderingData = tile.getRenderingData();
				String str = (String) renderingData.get("text");
				@SuppressWarnings("unchecked")
				List<String> codes = (List<String>) renderingData
						.get("text-codes");
				if ((Boolean) renderingData.get("character")) {
					str = String.format(this.characterText, str);
				}
				this.outputColumn(str, codes);
			}
			this.writer.println();
			this.writer.println();
		}
	}

	public void renderRoundAttributes(Engine engine) {
		this.outputStringObjectMap(engine.getAttributes());
	}

	public void outputColumn(String str) {
		this.outputColumn(str, null);
	}

	public void outputColumn(String str, List<String> codes) {
		this.outputFixedWidth(str, this.columnWidth, codes);
	}

	public void outputFixedWidth(String str, int width, List<String> codes) {
		if (str.length() > width) {
			str = str.substring(str.length() - width);
		}
		int spaces = width - str.length();
		int rightSpaces = spaces >> 1;
		int leftSpaces = rightSpaces + (spaces & 1);
		this.outputSpaces(leftSpaces);
		this.outputString(str, codes);
		this.outputSpaces(rightSpaces);
	}

	public void outputString(String str, List<String> codes) {
		boolean useCodes = (codes != null && this.isConsole);
		if (useCodes) {
			for (String code : codes) {
				String codeStr = ansiCodes.get(code);
				if (codeStr != null) {
					this.writer.print("\u001b[" + codeStr);
				}
			}
		}
		this.writer.print(str);
		if (useCodes) {
			this.writer.print("\u001b[0m");
		}
	}

	public void outputSpaces(int spaces) {
		for (int i = 0; i < spaces; i++) {
			this.writer.print(' ');
		}
	}

	public void outputStringObjectMap(Map<String, Object> map) {
		for (Map.Entry<String, Object> attributeEntry : map.entrySet()) {
			this.writer.format("%s: %s;\t", attributeEntry.getKey(),
					attributeEntry.getValue());
		}
		this.writer.println();
		this.writer.println();
	}

	@SuppressWarnings("unchecked")
	public void renderEvent(StandardEvent event) {
		if (event != null) {
			this.writer.println("OK.");
		} else {
			this.writer.println("REJECTED.");
		}
		this.writer.println();
		if (event != null
				&& event.getExtraInformation().containsKey("fight-details")) {
			this.writer.println("Fight details:");
			for (Map<String, Object> fightDetails : (List<Map<String, Object>>) event
					.getExtraInformation().get("fight-details")) {
				this.writer.print("* ");
				this.outputStringObjectMap(fightDetails);
			}
		}
		if (event != null
				&& event.getExtraInformation().containsKey("messages")) {
			this.writer.println("Messages:");
			for (String message : (List<String>) event.getExtraInformation()
					.get("messages")) {
				this.writer.format("* %s\n", message);
			}
			this.writer.println();
		}
	}
}
