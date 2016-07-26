import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Samples the performance of this sliding max algorithm. I think this can be modified
 * and used for our Steering Angle (swerving) rule.
 * 
 * @author spenc_000
 *
 */
public class SlidingMax {

	public static void main(String[] args) {

		int[] A = { 12, 23, 57, 17, 23, 34, 1, 9, 26, 27, 42, 4, 8, 15, 16, 23,
				42 };
		int[] B = new int[A.length];
		int n = A.length;
		int w = 5;

		maxSlidingWindow(A, n, w, B);

		System.out.println("A has " + A.length + " elements.");
		System.out.println("The window size is " + w + ".");
		System.out.println();

		for (int i = 0; i < n; i++) {
			System.out.println(B[i]);
		}
	}

	/*
	 * entire array of elements is A, n is number of elements in A, w is the window size
	 * (which will be in terms of time for us), B[i] is an array that will store the
	 * maximum in the window A[i] to A[i+w]
	 */
	static void maxSlidingWindow(int A[], int n, int w, int B[]) {

		Deque<Integer> Q; // create a double-ended queue structure
		Q = new ArrayDeque<Integer>();

		for (int i = 0; i < w; i++) { // starting at index zero, up to w-1...

			while (!Q.isEmpty() && A[i] >= A[Q.peekLast()])
				// while the deque is NOT empty, and the ith element in A is >=
				// the A[back of deque]...
				Q.removeLast(); // remove 1 element from the back of the queue
			Q.addLast(i); // add 1 element to back of the queue
		}
		for (int i = w; i < n; i++) { // starting with the wth element, going up
										// to n-1
			B[i - w] = A[Q.peekFirst()]; // B[i-w] stores the element in A at
											// index
											// saved in front of queue
			while (!Q.isEmpty() && A[i] >= A[Q.peekLast()])
				// while the queue is not empty and the ith element of A is >=
				// element in A with index stored at back of queue
				Q.removeLast(); // remove 1 element from the back of the queue
			while (!Q.isEmpty() && Q.peekFirst() <= i - w)
				// while queue is not empty and front element in queue is <= i-w
				Q.removeFirst(); // remove one element from the front of the
									// queue
			Q.addLast(i); // push the value of index i onto the back of the
							// queue
		}
		B[n - w] = A[Q.peekFirst()]; // store A[value at front of queue] into B[n-w]
	}

}
