// ===================================================================================
// AutomatedCarParking
// ===================================================================================

	1) Running the Program
	2) Using a Different FuzzifyKernel
	3) Issues

// ===================================================================================
// 1) Running The Program
// ===================================================================================

	Running the program is as trivial as executing the ff. command:

		`AutoCP KernelName`
	
	**Running the program without kernel VIOLATES the requirements,
	as it uses the default pseudo-fuzzify kernel. It's only purpose
	is to provide quick testing of the system.

// ===================================================================================
// 2) Using a Different FuzzifyKernel
// ===================================================================================

	In the home folder, there is the directory `automatedcarparking` and
	`kernel`. The `automatedcarparking` contains the class file for the
	kernel interface for convenience, so you can compile kernels from the
	root directory simply by:

		`javac kernel/NewKernel.java`

	Note that as usual, you to import FuzzifyKernel in `NewKernel.java`

		`import automatedcarparking.FuzzifyKernel;`

	After that, you should run the bootstrap loader with the class file
	as the *FIRST* argument. Refer to running the program for more info.

		`AutoCP NewKernel`
	
	**You should put the kernels in the kernel library so it can be loaded.

// ===================================================================================
// 3) Issues
// ===================================================================================

	If you are getting a WGL error stating that your driver does not
	support opengl, recommended java version is 1.8.0_45.

	It is the java version I compiled with. I get errors on 1.8.0_101
	and possibly all version above 0_45.

	Ask Kirc for assistance regarding on how to fix this issue.

	