/**
 * A simple generic tuple type that I like to use when I have a hard time splitting two return values
 * into two functions. I find it preferable to return a tuple than introducing mutable fields into the class.
 *
 * @param <L> left type
 * @param <R> right type
 */
class Tuple<L, R> {
    private final L left;
    private final R right;

    Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    L getLeft() {
        return left;
    }

    R getRight() {
        return right;
    }
}
