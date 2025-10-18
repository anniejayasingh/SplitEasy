package com.spliteasy.expense_sharing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spliteasy.expense_sharing.dto.SettlementRequestDTO;
import com.spliteasy.expense_sharing.entity.Settlement;
import com.spliteasy.expense_sharing.service.SettlementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing settlements.
 */
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Settlement Controller", description = "Operations related to settlements")

public class SettlementController {

	private final SettlementService settlementService;

	/**
	 * Settle a specific settlement by ID.
	 */
	@PostMapping("/{settlementId}/settle")
	@Operation(summary = "Settle an expense", description = "Records a settlement between two users")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Settlement recorded successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input") })
	public ResponseEntity<Settlement> settlePayment(@PathVariable Long settlementId, @RequestHeader("Authorization") String authHeader) {
		log.info("Request to settle settlementId={}", settlementId);
		
		Settlement settled = settlementService.settlePayment(settlementId,authHeader);
		
		return ResponseEntity.ok(settled);
	}

	/**
	 * Get all unsettled settlements for a specific user.
	 */
	@GetMapping("/user/{userId}")
	@Operation(summary = "Get settlements by user", description = "Fetches all settlements for a specific user")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Settlements fetched successfully"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<List<Settlement>> getUserUnsettled(@PathVariable Long userId,@AuthenticationPrincipal OAuth2User principal) {
		log.info("Fetching unsettled settlements for userId={}", userId);
		
		List<Settlement> settlements = settlementService.getUserUnsettled(userId);
		
		if (settlements.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(settlements);
	}

	/**
	 * Create a new settlement using a input request details.
	 */
	@PostMapping
	@Operation(summary = "Create a settlement", description = "Records a settlement between a payer and a receiver, updating balances accordingly")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Settlement successfully created"),
			@ApiResponse(responseCode = "400", description = "Invalid input or insufficient balance"),
			@ApiResponse(responseCode = "404", description = "Payer or receiver not found") })
	public ResponseEntity<Settlement> createSettlement(@RequestBody SettlementRequestDTO request) {
		log.info("Creating new settlement: {}", request);
		
		Settlement settlement = settlementService.createSettlement(request.getFromUserId(), request.getToUserId(),
				request.getExpenseId(), request.getAmount());
		
		return ResponseEntity.ok(settlement);
	}
}
