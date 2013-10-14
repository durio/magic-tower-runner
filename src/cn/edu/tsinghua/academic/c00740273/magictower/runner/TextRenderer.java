package cn.edu.tsinghua.academic.c00740273.magictower.runner;

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

	protected int columnWidth;
	protected String characterText;

	@Override
	public void initialize(JSONObject dataValue) throws JSONException,
			DataFormatException {
		this.columnWidth = dataValue.getInt("column-width");
		this.characterText = dataValue.getString("character-text");
	}

	public void renderRoundEvent(Engine engine, StandardEvent event) {
		System.out.println("\u001b[2J\u001b[H");
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
		System.out.format("CUR (%d, %d, %d) MAX (%d, %d, %d) =[%d]=\n\n",
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
		System.out.println();
		System.out.println();

		// The rest part.
		Tile[][] tiles = engine.getLayerTiles(currentCoord.getZ());
		for (int i = 0; i <= maxCoord.getX(); i++) {
			this.outputColumn(String.valueOf(i));
			StandardTile[] tileRow = (StandardTile[]) tiles[i];
			for (StandardTile tile : tileRow) {
				Map<String, Object> renderingData = tile.getRenderingData();
				String str = (String) renderingData.get("text");
				if ((Boolean) renderingData.get("character")) {
					str = String.format(this.characterText, str);
				}
				this.outputColumn(str);
			}
			System.out.println();
			System.out.println();
		}
	}

	public void renderRoundAttributes(Engine engine) {
		this.outputStringObjectMap(engine.getAttributes());
	}

	public void outputColumn(String str) {
		this.outputFixedWidth(str, this.columnWidth);
	}

	public void outputFixedWidth(String str, int width) {
		if (str.length() > width) {
			str = str.substring(str.length() - width);
		}
		int spaces = width - str.length();
		int rightSpaces = spaces >> 1;
		int leftSpaces = rightSpaces + (spaces & 1);
		this.outputSpaces(leftSpaces);
		System.out.print(str);
		this.outputSpaces(rightSpaces);
	}

	public void outputSpaces(int spaces) {
		for (int i = 0; i < spaces; i++) {
			System.out.print(' ');
		}
	}

	public void outputStringObjectMap(Map<String, Object> map) {
		for (Map.Entry<String, Object> attributeEntry : map.entrySet()) {
			System.out.format("%s: %s;\t", attributeEntry.getKey(),
					attributeEntry.getValue());
		}
		System.out.println();
		System.out.println();
	}

	@SuppressWarnings("unchecked")
	public void renderEvent(StandardEvent event) {
		if (event != null) {
			System.out.println("OK.");
		} else {
			System.out.println("REJECTED.");
		}
		System.out.println();
		if (event != null
				&& event.getExtraInformation().containsKey("fight-details")) {
			System.out.println("Fight details:");
			for (Object fightDetails : (List<Object>) event
					.getExtraInformation().get("fight-details")) {
				System.out.print("* ");
				this.outputStringObjectMap((Map<String, Object>) fightDetails);
			}
		}
	}
}
