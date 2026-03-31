package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;

public enum RouterStyle {
	DEFAULT,
	VERTICAL_RIGHT_ANGLE,
	HORIZONTAL_RIGHT_ANGLE,
	VERTICAL_LIGHTNING,
	HORIZONTAL_LIGHTNING,
	VERTICAL_HOMOLATERAL,
	HORIZONTAL_HOMOLATERAL;

	public static String[] getDisplayNames(RouterStyle...styls) {
		String[] names = new String[styls.length];
		int i = 0;
		for(RouterStyle style: styls)
			names[i++] = style.getDisplayName();
		return names;
	}

	public String getDisplayName() {
		return name().replace("_", " ").toLowerCase();
	}

	public static RouterStyle findByDisplayName(String displayName) {
		return valueOf(displayName.replace(" ", "_").toUpperCase());
	}

	public ConnectionRouter create() {
		switch (this) {
			case DEFAULT: return new DefaultRouter();
			case VERTICAL_RIGHT_ANGLE: return new RightAngleRouter(true);
			case HORIZONTAL_RIGHT_ANGLE: return new RightAngleRouter(false);
			case VERTICAL_LIGHTNING: return new LightningRouter(true);
			case HORIZONTAL_LIGHTNING: return new LightningRouter(false);
			case VERTICAL_HOMOLATERAL: return new HomolateralRouter(true);
			case HORIZONTAL_HOMOLATERAL: return new HomolateralRouter(false);
			default: throw new IllegalArgumentException();
		}
	}

	public ConnectionLocator createLocator(Figure figure) {
		switch (this) {
			case DEFAULT: return new DefaultLocator(figure);
			case VERTICAL_RIGHT_ANGLE: return new RightAngleLocator(figure, true);
			case HORIZONTAL_RIGHT_ANGLE: return new RightAngleLocator(figure, false);
			case VERTICAL_LIGHTNING: return new LightningLocator(figure, true);
			case HORIZONTAL_LIGHTNING: return new LightningLocator(figure, false);
			case VERTICAL_HOMOLATERAL: return new LightningLocator(figure, true);
			case HORIZONTAL_HOMOLATERAL: return new LightningLocator(figure, false);
			default: throw new IllegalArgumentException();
		}
	}

	public boolean isVertical() {
		return this == VERTICAL_LIGHTNING || this == VERTICAL_RIGHT_ANGLE || this == VERTICAL_HOMOLATERAL;
	}
}
