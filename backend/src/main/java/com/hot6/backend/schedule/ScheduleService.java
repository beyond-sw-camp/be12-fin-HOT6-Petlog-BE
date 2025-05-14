package com.hot6.backend.schedule;

import com.hot6.backend.category.model.Category;
import com.hot6.backend.category.model.CategoryRepository;
import com.hot6.backend.chat.model.ChatDto;
import com.hot6.backend.chat.model.ChatRoom;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.pet.PetRepository;
import com.hot6.backend.pet.SharedSchedulePetService;
import com.hot6.backend.pet.model.Pet;
import com.hot6.backend.pet.model.SharedSchedulePet;
import com.hot6.backend.schedule.model.Schedule;
import com.hot6.backend.schedule.model.ScheduleDto;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final CategoryRepository categoryRepository;
    private final PetRepository petRepository;
    private final SharedSchedulePetService sharedSchedulePetService;

    @Transactional
    public void createSchedule(Long petIdx, ScheduleDto.ScheduleCreateRequest dto) {
        Category category = categoryRepository.findById(dto.getCategoryIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        Pet pet = petRepository.findById(petIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PET_NOT_FOUND));

        if (!dto.isRecurring()) {
            Schedule schedule = dto.toEntity(category, pet);
            scheduleRepository.save(schedule);
            return;
        }

        LocalDateTime startAt = dto.getStartAt();
        LocalDateTime endAt = dto.getEndAt();
        LocalDate repeatEndAt = dto.getRepeatEndAt();
        int repeatCount = dto.getRepeatCount();
        String repeatCycle = dto.getRepeatCycle();

        while (!startAt.toLocalDate().isAfter(repeatEndAt)) {
            Schedule newSchedule = Schedule.builder()
                    .sTitle(dto.getTitle())
                    .sMemo(dto.getMemo())
                    .categoryIdx(category.getIdx())
                    .startAt(startAt)
                    .endAt(endAt)
                    .recurring(true)
                    .repeatCycle(repeatCycle)
                    .repeatCount(repeatCount)
                    .repeatEndAt(repeatEndAt)
                    .placeName(dto.getPlaceName())
                    .placeId(dto.getPlaceId())
                    .pet(pet)
                    .build();

            scheduleRepository.save(newSchedule);

            switch (repeatCycle) {
                case "일":
                    startAt = startAt.plusDays(repeatCount);
                    endAt = endAt.plusDays(repeatCount);
                    break;
                case "주":
                    startAt = startAt.plusWeeks(repeatCount);
                    endAt = endAt.plusWeeks(repeatCount);
                    break;
                case "월":
                    startAt = startAt.plusMonths(repeatCount);
                    endAt = endAt.plusMonths(repeatCount);
                    break;
                default:
                    throw new BaseException(BaseResponseStatus.SCHEDULE_INVALID_REPEAT_CYCLE);
            }
        }
    }

    public List<ScheduleDto.SimpleSchedule> getAllSchedule(Long userIdx) {
        List<ScheduleDto.SimpleSchedule> scheduleList = new ArrayList<>();
        List<Pet> pets = petRepository.findByUserIdx(userIdx);

        for (Pet pet : pets) {
            List<Schedule> schedules = scheduleRepository.findAllByPet(pet);

            for (Schedule schedule : schedules) {
                Category category = categoryRepository.findById(schedule.getCategoryIdx())
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
                scheduleList.add(ScheduleDto.SimpleSchedule.from(schedule, category));
            }
        }
        return scheduleList;
    }

    public Schedule getSchedule(Long scheduleIdx) {
        return scheduleRepository.findById(scheduleIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.SCHEDULE_NOT_FOUND));
    }

    public List<ChatDto.ChatRoomScheduleElement> getALLScheduleByChatRoom(Long chatRoomIdx) {
        return scheduleRepository.findAllWithChatRoomByChatRoomIdx(chatRoomIdx).stream().map(ChatDto.ChatRoomScheduleElement::from).collect(toList());
    }

    @Transactional
    public void createChatRoomSchedule(ChatDto.CreateChatRoomScheduleRequest dto, ChatRoom chatRoom, User user) {
        Category category = categoryRepository.findById(dto.getCategoryIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
        scheduleRepository.save(dto.toEntity(chatRoom, category));
    }

    public List<User> findChatRoomUsersParticipatingInSchedule(Long scheduleIdx) {
        return scheduleRepository.findChatRoomUsersParticipatingInSchedule(scheduleIdx);
    }

    public List<ScheduleDto.SimpleSchedule> getSchedulesByDate(Long userIdx, Integer year, Integer month, Integer day) {
        LocalDate date = LocalDate.of(year, month, day);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX); // 23:59:59.999...

        List<ScheduleDto.SimpleSchedule> scheduleList = new ArrayList<>();

        List<Pet> pets = petRepository.findByUserIdx(userIdx);

        for (Pet pet : pets) {
            List<Schedule> schedules = scheduleRepository.findAllByPetAndOverlappingDate(pet, start, end);

            for (Schedule schedule : schedules) {
                Category category = categoryRepository.findById(schedule.getCategoryIdx())
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
                scheduleList.add(ScheduleDto.SimpleSchedule.from(schedule, category));
            }
        }

        return scheduleList;
    }

    public List<ScheduleDto.SimpleSchedule> getSchedulesByPet(Long petIdx) {
        List<ScheduleDto.SimpleSchedule> scheduleList = new ArrayList<>();

        Pet pet = petRepository.findById(petIdx).orElseThrow(() ->
                new BaseException(BaseResponseStatus.PET_NOT_FOUND));

        List<Schedule> schedules = scheduleRepository.findAllByPet(pet);

        for (Schedule schedule : schedules) {
            Category category = categoryRepository.findById(schedule.getCategoryIdx())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
            scheduleList.add(ScheduleDto.SimpleSchedule.from(schedule, category));
        }

        return scheduleList;
    }

    public List<ScheduleDto.SimpleSchedule> getSchedulesByPetAndDate(Long petIdx, Integer year, Integer month, Integer day) {
        LocalDate date = LocalDate.of(year, month, day);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX); // 23:59:59.999...

        List<ScheduleDto.SimpleSchedule> scheduleList = new ArrayList<>();

        Pet pet = petRepository.findById(petIdx).orElseThrow(() ->
                new BaseException(BaseResponseStatus.PET_NOT_FOUND));

        List<Schedule> schedules = scheduleRepository.findAllByPetAndOverlappingDate(pet, start, end);

        for (Schedule schedule : schedules) {
            Category category = categoryRepository.findById(schedule.getCategoryIdx())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
            scheduleList.add(ScheduleDto.SimpleSchedule.from(schedule, category));

        }

        return scheduleList;
    }

    public ScheduleDto.ScheduleDetail getScheduleDetail(Long scheduleIdx) {
        Schedule schedule = scheduleRepository.findById(scheduleIdx).orElseThrow(
                () -> new BaseException(BaseResponseStatus.SCHEDULE_NOT_FOUND));

        Category category = categoryRepository.findById(schedule.getCategoryIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));
        return ScheduleDto.ScheduleDetail.from(schedule, category);

    }

    @Transactional
    public void updatePetSchedule(User user, Long petIdx, Long scheduleIdx,ScheduleDto.ScheduleUpdateRequest dto) {
        Schedule schedule = scheduleRepository.findScheduleByPet_Idx(petIdx).orElseThrow(() -> new BaseException(BaseResponseStatus.SCHEDULE_NOT_FOUND));

        schedule.update(
                dto.getCategoryIdx(),
                dto.getTitle(),
                dto.getPlaceName(),
                dto.getMemo(),
                dto.getStartAt(),
                dto.getEndAt()
        );
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void deletePetSchedule(Long petIdx, Long scheduleIdx) {
        Schedule schedule = scheduleRepository.findScheduleByPet_Idx(petIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.SCHEDULE_NOT_FOUND));

        scheduleRepository.delete(schedule);
    }
}
