package com.github.zzwwws.plugig.compatible;

import android.text.TextUtils;

import java.lang.reflect.Field;

final class FieldAccessor {
	private boolean prepared;
	private Field field;
	private String className;
	private String fieldName;
	private Object object;

	public FieldAccessor(Object object, String fieldName, String className) {
		if (object == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}

		this.object = object;
		this.fieldName = fieldName;
		this.className = className;
	}

	private void prepare() {
		if (prepared) {
			return;
		}
		prepared = true;

		Class<?> clazz = object.getClass();
		while (clazz != Object.class) {
			if (TextUtils.isEmpty(className)) {
				try {
					Field field = clazz.getDeclaredField(fieldName);
					field.setAccessible(true);
					this.field = field;
				} catch (NoSuchFieldException ex) {
					ex.printStackTrace();
				}
			} else {
				for (Field field : clazz.getDeclaredFields()) {
					if (field.getType().getName().equals(className)) {
						field.setAccessible(true);
						this.field = field;
					}
				}
			}

			clazz = clazz.getSuperclass();
		}
	}

	public final Object get() throws NoSuchFieldException,
			IllegalAccessException, IllegalArgumentException {
		prepare();

		if (this.field == null) {
			throw new NoSuchFieldException();
		}

		try {
			return this.field.get(this.object);
		} catch (ClassCastException ex) {
			ex.printStackTrace();
		}

		throw new IllegalArgumentException("unable to cast object");
	}

	public final void set(Object obj) throws NoSuchFieldException,
			IllegalAccessException, IllegalArgumentException {
		prepare();

		if (this.field == null) {
			throw new NoSuchFieldException();
		}

		this.field.set(this.object, obj);
	}
}