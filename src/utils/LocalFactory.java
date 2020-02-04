package utils;

public interface LocalFactory<T> {
	T create(Object... args);
}
