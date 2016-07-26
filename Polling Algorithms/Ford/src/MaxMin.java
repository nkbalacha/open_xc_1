import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Samples the performance of this sliding max algorithm. I think this can be modified
 * and used for our Steering Angle (swerving) rule.
 * 
 * @author spenc_000
 *
 */
public class MaxMin {

	public static void main(String[] args) {

		int[] A = { 12, 23, 57, 17, 23, 34, 1, 9 };
		int[] maxes = new int[A.length];
		int[] mins = new int[A.length];
		int n = A.length;
		int w = 4;

		slidingMaxMin(A, n, w, maxes, mins);

		System.out.println("A has " + A.length + " elements.");
		System.out.println("The window size is " + w + ".");
		System.out.println();

		System.out.println();
		System.out.println("Maximums:  Minimums:");

		for (int i = 0; i < n; i++) {
			System.out.println(maxes[i] + "          " + mins[i]);
			if (maxes[i] - mins[i] >= 43)
				System.out.println("SWERVE.");
		}
	}

	/*
	 * entire array of elements is A, n is number of elements in A, w is the window size
	 * (which will be in terms of time for us), B[i] is an array that will store the
	 * maximum in the window A[i] to A[i+w]
	 */
	static void slidingMaxMin(int A[], int n, int w, int maxes[], int mins[]) {

		Deque<Integer> maxQ; // create a double-ended queue structure
		Deque<Integer> minQ; // store mins separately from maxes
		maxQ = new ArrayDeque<Integer>();
		minQ = new ArrayDeque<Integer>();

		for (int i = 0; i < w; i++) { // starting at index zero, up to w-1...

			// ------------MAX BLOCK----------------------------------------

			while (!maxQ.isEmpty() && A[i] >= A[maxQ.peekLast()])
				// while the deque is NOT empty, and the ith element in A is >=
				// the A[back of deque]...
				maxQ.removeLast(); // remove 1 element from the back of the queue
			maxQ.addLast(i); // add 1 element to back of the queue

			// ------------MIN BLOCK----------------------------------------

			/* repeat the analogy of this while loop for the mins */
			while (!minQ.isEmpty() && A[i] <= A[minQ.peekLast()])
				// while the deque is NOT empty, and the ith element in A is >=
				// the A[back of deque]...
				minQ.removeLast(); // remove 1 element from the back of the queue
			minQ.addLast(i); // add 1 element to back of the queue
		}
		for (int i = w; i < n; i++) { // starting with the wth element, going up
										// to n-1
			maxes[i - w] = A[maxQ.peekFirst()];
			mins[i - w] = A[minQ.peekFirst()];

			// -----------MAX BLOCK-------------------------------------------

			while (!maxQ.isEmpty() && A[i] >= A[maxQ.peekLast()])
				// while the queue is not empty and the ith element of A is >=
				// element in A with index stored at back of queue
				maxQ.removeLast(); // remove 1 element from the back of the queue
			while (!maxQ.isEmpty() && maxQ.peekFirst() <= i - w)
				// while queue is not empty and front element in queue is <= i-w
				maxQ.removeFirst(); // remove one element from the front of the
									// queue
			maxQ.addLast(i); // push the value of index i onto the back of the
								// queue

			// -----------MIN BLOCK-------------------------------------------

			while (!minQ.isEmpty() && A[i] <= A[minQ.peekLast()])
				// while the queue is not empty and the ith element of A is >=
				// element in A with index stored at back of queue
				minQ.removeLast(); // remove 1 element from the back of the queue
			while (!minQ.isEmpty() && minQ.peekFirst() <= i - w)
				// while queue is not empty and front element in queue is <= i-w
				minQ.removeFirst(); // remove one element from the front of the
									// queue
			minQ.addLast(i); // push the value of index i onto the back of the
								// queue

		}
		maxes[n - w] = A[maxQ.peekFirst()]; // store A[value at front of queue] into
		mins[n - w] = A[minQ.peekFirst()]; // B[n-w]
	}

}
