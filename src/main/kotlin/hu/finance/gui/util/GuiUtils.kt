package hu.finance.gui.util

import java.util.concurrent.locks.Lock
import javax.swing.SwingWorker

class AutoCloseableLock(
    private val lock: Lock
) : Lock by lock {
    fun withLock(block: () -> Unit) {
        lock.lock()
        try {
            block.invoke()
        } finally {
            lock.unlock()
        }
    }
}

class CalcWorker<T, R>(
    private val loader: () -> T,
    private val callback: (T) -> Unit
) : SwingWorker<T, R>() {
    override fun doInBackground(): T = loader.invoke()
    override fun done() = callback.invoke(get())
}
