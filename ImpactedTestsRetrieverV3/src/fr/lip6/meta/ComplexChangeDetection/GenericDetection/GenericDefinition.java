package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;

public interface GenericDefinition {

	public boolean doesContain(AtomicChange element);	
	public boolean doesContain(ComplexChange element);
	public TemporaryComplexChange create();
}
