/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.prediction.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

/**
 * ViewModel containing all the logic needed to run the game
 * ViewModel, содержащий всю логику, необходимую для запуска игры
 */
class GameViewModel : ViewModel() {

    // These are the three different types of buzzing in the game. Buzz pattern is the number of
    // milliseconds each interval of buzzing and non-buzzing takes.
    // В игре есть три разных типа жужжания. Шаблон жужжания — это количество
    // миллисекунд, которое длится каждый интервал жужжания и отсутствия жужжания.
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    companion object {
        // These represent different important times in the game, such as game length.
        // Они отображают различные важные моменты в игре, например, продолжительность игры

        // This is when the game is over
        // Вот когда игра окончена
        private const val DONE = 0L

        // This is the time when the phone will start buzzing each second
        // Это время, когда телефон начнет вибрировать каждую секунду
        private const val COUNTDOWN_PANIC_SECONDS = 10L

        // This is the number of milliseconds in a second
        // Это количество миллисекунд в секунде
        private const val ONE_SECOND = 1000L

        // This is the total time of the game
        // Это общее время игры
        private const val COUNTDOWN_TIME = 5000L

    }

    private val timer: CountDownTimer

    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime

    // The String version of the current time
    // Строковая версия текущего времени
    val currentTimeString: LiveData<String> = currentTime.map { time ->
        DateUtils.formatElapsedTime(time)
    }

    // The current word
    // Текущее слово
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word


    // The current score
    // Текущий счёт
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score


    // The list of words - the front of the list is the next word to guess
    // Список слов. В начале списка находится следующее слово, которое нужно угадать
    private lateinit var wordList: MutableList<String>

    // Event which triggers the end of the game
    // Событие, которое запускает конец игры
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish

    // Event that triggers the phone to buzz using different patterns, determined by BuzzType
    // Событие, которое заставляет телефон вибрировать по разным шаблонам, определяемым BuzzType.
    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz

    init {
        resetList()
        nextWord()
        _score.value = 0

        // Creates a timer which triggers the end of the game when it finishes
        //Создает таймер, который активирует окончание игры по ее завершении.
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (millisUntilFinished / ONE_SECOND)
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _eventBuzz.value = BuzzType.GAME_OVER
                _eventGameFinish.value = true
            }
        }

        timer.start()
    }

    /**
     * Resets the list of words and randomizes the order
     * Сбрасывает список слов и располагает их в случайном порядке.
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     * Переход к следующему слову в списке
     */
    private fun nextWord() {
        //Select and remove a word from the list
        //Выбрать и удалить слово из списка
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /** Methods for buttons presses
     * Методы нажатия кнопок **/

    fun onSkip() {
        _score.value = (_score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (_score.value)?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        nextWord()
    }

    /** Methods for completed events
     * Методы для завершенных событий **/

    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}
