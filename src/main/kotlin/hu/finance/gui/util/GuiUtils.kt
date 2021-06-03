package hu.finance.gui.util

import java.util.concurrent.locks.Lock
import javax.swing.SwingWorker

class AutoCloseableLock(
    private val lock: Lock
) : Lock by lock {
    fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block.invoke()
        } finally {
            lock.unlock()
        }
    }
}

class CalcWorkerResponse<T>(
    val result: Boolean,
    val error: Exception? = null,
    val data: T? = null
)

class CalcWorker<T, R>(
    private val loader: () -> T,
    private val callback: (CalcWorkerResponse<T>) -> Unit
) : SwingWorker<T, R>() {
    override fun doInBackground(): T = loader.invoke()
    override fun done() {
        val resp = try {
            CalcWorkerResponse(result = true, data = get())
        } catch (ex: Exception) {
            CalcWorkerResponse(result = false, error = ex)
        }
        callback.invoke(resp)
    }
}
