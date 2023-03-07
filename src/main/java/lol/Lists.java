package lol;

import java.util.ArrayList;
import java.util.Collections;

public class Lists{
	@SafeVarargs
	public static <E> ArrayList<E> newArrayList(E... elements){
		ArrayList<E> l = new ArrayList<>();
		Collections.addAll(l, elements);
		return l;
	}
}
