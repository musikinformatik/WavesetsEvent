+ Environment {

	useWithoutParents { |func|
		var oldParent = currentEnvironment.parent;
		currentEnvironment.parent = nil;
		this.use(func);
		currentEnvironment.parent = oldParent;
	}

}

