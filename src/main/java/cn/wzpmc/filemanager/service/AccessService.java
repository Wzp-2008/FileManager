package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.AccessInformation;
import cn.wzpmc.filemanager.mapper.AccessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccessService {
    private final AccessMapper mapper;
    @Autowired
    public AccessService(AccessMapper mapper){
        this.mapper = mapper;
        this.mapper.createDefault();
    }

    public void addAccessCounter() {
        if (this.mapper.countToday() == 0){
            this.mapper.addToday();
        }
        this.mapper.addTodayAccess();
    }

    public void addDownloadCounter() {
        if (this.mapper.countToday() == 0){
            this.mapper.addToday();
        }
        this.mapper.addTodayDownload();
    }

    public List<AccessInformation> getAccessInformation(int count){
        List<AccessInformation> allAccessData = this.mapper.getAllAccessData(count);
        Map<Date, AccessInformation> resultMap = new HashMap<>();
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < count; i++) {
            Date date = instance.getTime();
            resultMap.put(date, new AccessInformation(0,0, date));
            instance.add(Calendar.DAY_OF_MONTH, -1);
        }
        for (AccessInformation allAccessDatum : allAccessData) {
            Date date = allAccessDatum.getDate();
            resultMap.put(date, allAccessDatum);
        }
        ArrayList<AccessInformation> result = new ArrayList<>(resultMap.values());
        result.sort((a, b) -> Math.toIntExact(a.getDate().getTime() - b.getDate().getTime()));
        return result;
    }

    public AccessInformation getAllAccessInformation() {
        return this.mapper.getAllAccessInformation();
    }
}
