/* ProcessorChain
 *
 * $Id: ProcessorChain.java 4434 2006-08-04 04:02:39Z gojomo $
 *
 * Created on Mar 1, 2004
 *
 * Copyright (C) 2004 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package bin.spider.frame;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class groups together a number of processors that logically fit
 * together.
 *
 * @author John Erik Halse
 */
public class ProcessorChain {
    private static Logger logger =
        LoggerFactory.getLogger("org.archive.crawler.framework.ProcessorChain");

    private final List<Processor> processorList;
    private ProcessorChain nextChain;
    private Processor firstProcessor;

    /** Construct a new processor chain.
     *
     * @param processorMap a map of the processors belonging to this chain.
     */
    public ProcessorChain(List<Processor> processorList) {
        this.processorList = processorList;

        Processor previous = null;
//        for (Processor processor : processorList) {
//        	if (previous == null) {
//                firstProcessor = processor;
//            } else {
//                previous.setDefaultNextProcessor(processor);
//            }
//
//            previous = processor;
//		}
        for (Iterator it = processorList.iterator(); it.hasNext();) {
            Processor p = (Processor) it.next();

            if (previous == null) {
                firstProcessor = p;
            } else {
                previous.setDefaultNextProcessor(p);
            }

            logger.info(
                "Processor: " + p.getName() + " --> " + p.getClass().getName());

            previous = p;
        }
    }

    /** Set the processor chain that the URI should be working through after
     * finishing this one.
     *
     * @param nextProcessorChain the chain that should be processed after this
     *        one.
     */
    public void setNextChain(ProcessorChain nextProcessorChain) {
        this.nextChain = nextProcessorChain;
    }

    /** Get the processor chain that the URI should be working through after
     * finishing this one.
     *
     * @return the next processor chain.
     */
    public ProcessorChain getNextProcessorChain() {
        return nextChain;
    }

    /** Get the first processor in the chain.
     *
     * @return the first processor in the chain.
     */
    public Processor getFirstProcessor() {
        return firstProcessor;
    }

    /** Get the first processor that is of class <code>classType</code> or a
     * subclass of it.
     *
     * @param classType the class of the requested processor.
     * @return the first processor matching the classType.
     */
    public Processor getProcessor(Class classType) {
//    	for (Processor processor : processorList) {
//    		if (classType.isInstance(processor)) {
//              return processor;
//          }
//		}
        for (Iterator it = processorList.iterator(); it.hasNext();) {
            Processor p = (Processor) it.next();
            if (classType.isInstance(p)) {
                return p;
            }
        }
        return null;
    }

    /** Get the number of processors in this chain.
     *
     * @return the number of processors in this chain.
     */
    public int size() {
    	return processorList.size();
//        return processorMap.size(null);
    }

    /** Get an iterator over the processors in this chain.
     *
     * @return an iterator over the processors in this chain.
     */
    public Iterator iterator() {
        return processorList.iterator();
    }

    public void kickUpdate() {
//    	for (Processor processor : processorList) {
//    		processor.kickUpdate(); 
//    	}
        Iterator iter = iterator();
        while(iter.hasNext()) {
            Processor p = (Processor) iter.next(); 
            p.kickUpdate(); 
        }
    }
}
