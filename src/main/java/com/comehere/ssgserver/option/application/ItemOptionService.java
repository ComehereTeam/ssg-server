package com.comehere.ssgserver.option.application;

import java.util.List;

import com.comehere.ssgserver.option.domain.Color;
import com.comehere.ssgserver.option.dto.ColorRespDTO;
import com.comehere.ssgserver.option.dto.EtcRespDTO;
import com.comehere.ssgserver.option.dto.ItemOptionRespDTO;
import com.comehere.ssgserver.option.dto.OptionRespDTO;
import com.comehere.ssgserver.option.dto.SizeRespDTO;

public interface ItemOptionService {
	ItemOptionRespDTO findByItemId(Long itemId);

	OptionRespDTO hasOptions(Long itemId);

	ColorRespDTO getColors(Long itemId);

	SizeRespDTO getSizes(Long itemId, Long colorId);

	EtcRespDTO getEtcs(Long itemId, Long colorId, Long sizeId);
}
