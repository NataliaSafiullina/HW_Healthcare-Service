package ru.netology.patient.service.medical;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MedicalServiceImplTest {

    // Создадим поток аргументов, это т.н. фабричный метод
    public static Stream<Arguments> argsIdBloodPressureTemp() {
        return Stream.of(
                Arguments.of("001",
                        new BloodPressure(120, 80), new BloodPressure(130, 90),
                        BigDecimal.valueOf(36.6), BigDecimal.valueOf(30)),
                Arguments.of("002",
                        new BloodPressure(120, 80), new BloodPressure(110, 70),
                        BigDecimal.valueOf(36.6), BigDecimal.valueOf(35))
        );
    }


    /**
     * Проверить вывод сообщения во время проверки давления checkBloodPressure
     */
    @ParameterizedTest
    @MethodSource("argsIdBloodPressureTemp")
    void checkBloodPressureText(String id, BloodPressure goodBP, BloodPressure badBP) {

        // Создаём заглушку для PatientInfoFileRepository, типа по ID получаем данные по пациенту
        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById(id)).
                thenReturn(new PatientInfo(
                        id, "Пациент", "Нулевой",
                        LocalDate.of(1970, 1, 1),
                        new HealthInfo(new BigDecimal("36.6"), goodBP)
                ));

        // Создаем заглушку для SendAlertService
        SendAlertService sendAlertService = Mockito.mock(SendAlertService.class);

        // Задаем правильное значение сообщения
        String original = String.format("Warning, patient with id: %s, need help", id);

        // Вызываем метод, который тестируем
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure(id, badBP);

        // Сравним сообщения, для этого перехватим аргумент у метода send()
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        assertThat(original, is(argumentCaptor.getValue()));

        // Проверим, что вызвали метод 1 раз
        Mockito.verify(sendAlertService, Mockito.only()).send(original);
    }


    /**
     * Проверить, что сообщения не выводятся, когда показатели в норме.
     */
    @ParameterizedTest
    @MethodSource("argsIdBloodPressureTemp")
    void checkBloodPressureNever(String id, BloodPressure goodBP) {

        // Создаём заглушку для PatientInfoFileRepository, типа по ID получаем данные по пациенту
        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById(id)).
                thenReturn(new PatientInfo(
                        id, "Пациент", "Нулевой",
                        LocalDate.of(1970, 1, 1),
                        new HealthInfo(new BigDecimal("36.6"), goodBP)
                ));

        // Создаем заглушку для SendAlertService
        SendAlertService sendAlertService = Mockito.mock(SendAlertService.class);

        // Вызываем метод, который тестируем
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure(id, goodBP);

        // Проверяем что вызов отправки сообщения не сработал ни разу
        Mockito.verify(sendAlertService, Mockito.never()).send("any text");
    }


    /**
     * Проверить вывод сообщения во время проверки температуры checkTemperature
     */
    @ParameterizedTest
    @MethodSource("argsIdBloodPressureTemp")
    void checkTemperatureText(String id, BloodPressure goodBP, BloodPressure badBP, BigDecimal normTemp, BigDecimal notNormTemp) {

        // Создаём заглушку для PatientInfoFileRepository, типа по ID получаем данные по пациенту
        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById(id)).
                thenReturn(new PatientInfo(
                        id, "Пациент", "Нулевой",
                        LocalDate.of(1970, 1, 1),
                        new HealthInfo(normTemp, goodBP)
                ));

        // Создаем заглушку для SendAlertService
        SendAlertService sendAlertService = Mockito.mock(SendAlertService.class);

        // Задаем правильное значение сообщения
        String original = String.format("Warning, patient with id: %s, need help", id);

        // Вызываем метод, который тестируем
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkTemperature(id, notNormTemp);

        // Сравним сообщения, для этого перехватим аргумент у метода send()
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        assertThat(original, is(argumentCaptor.getValue()));

        // Проверим, что вызвали метод 1 раз
        Mockito.verify(sendAlertService, Mockito.only()).send(original);
    }
}